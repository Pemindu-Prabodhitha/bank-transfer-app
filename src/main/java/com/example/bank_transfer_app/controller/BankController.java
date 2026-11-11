package com.example.bank_transfer_app.controller;

import com.example.bank_transfer_app.dto.TransferRequest;
import com.example.bank_transfer_app.model.TransactionStatus;
import com.example.bank_transfer_app.service.BankService;
import com.example.bank_transfer_app.service.TransactionAuditService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.BigDecimal;

@Controller
@RequiredArgsConstructor
public class BankController {

    private final BankService bankService;
    private final TransactionAuditService auditService;

    @GetMapping("/")
    public String dashboard(@RequestParam(required = false) String keyword, Model model) {
        loadDashboard(model, keyword);
        if (!model.containsAttribute("transferRequest"))
            model.addAttribute("transferRequest", new TransferRequest());
        return "bank";
    }
    @GetMapping("/large-transfers")
    public String largeTransfers(@RequestParam(defaultValue = "10000") BigDecimal min, Model model) {
    model.addAttribute("min", min);
    model.addAttribute("transactions", bankService.largeTransfers(min));
    return "large";
    }

    @PostMapping("/customers")
    public String addCustomer(@RequestParam String accountNumber, @RequestParam String fullName,
                              @RequestParam(required = false) String email,
                              @RequestParam BigDecimal balance, RedirectAttributes ra) {
        try {
            bankService.createCustomer(accountNumber.trim(), fullName.trim(), email, balance);
            ra.addFlashAttribute("success", "Customer " + fullName + " added");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/";
    }

    @PostMapping("/transfer")
    public String transfer(@Valid @ModelAttribute("transferRequest") TransferRequest req,
                           BindingResult result, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {          // test 5: nothing saved
            loadDashboard(model, null);
            return "bank";
        }
        try {
            bankService.transfer(req.getFromAccount().trim(), req.getToAccount().trim(),
                                 req.getAmount(), req.isSimulateFailure());
            ra.addFlashAttribute("success", "Transfer of LKR " + req.getAmount() + " completed");
            return "redirect:/";
        } catch (Exception e) {
            // transfer() is already ROLLED BACK here; save FAILED row in a NEW transaction
            auditService.recordFailure(req.getFromAccount().trim(), req.getToAccount().trim(),
                                       req.getAmount(), e.getMessage());
            model.addAttribute("error", e.getMessage());
            loadDashboard(model, null);
            return "bank";
        }
    }
    @PostMapping("/deposit")
    public String deposit(@RequestParam String account,
                        @RequestParam BigDecimal amount,
                        RedirectAttributes ra) {
        try {
            bankService.deposit(account, amount);
            ra.addFlashAttribute("success", "Deposit of LKR " + amount + " completed");
        } catch (Exception e) {
            auditService.recordFailure(null, account, amount, e.getMessage());
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/";
    }

    @PostMapping("/withdraw")
    public String withdraw(@RequestParam String account,
                        @RequestParam BigDecimal amount,
                        RedirectAttributes ra) {
        try {
            bankService.withdraw(account, amount);
            ra.addFlashAttribute("success", "Withdrawal of LKR " + amount + " completed");
        } catch (Exception e) {
            auditService.recordFailure(account, null, amount, e.getMessage());
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/";
    }

    @GetMapping("/accounts/{accountNumber}/history")
    public String history(@PathVariable String accountNumber, Model model) {
        model.addAttribute("customer", bankService.getCustomer(accountNumber));
        model.addAttribute("transactions", bankService.history(accountNumber));
        model.addAttribute("totalSent", bankService.totalSent(accountNumber));
        return "history";
    }

    private void loadDashboard(Model model, String keyword) {
        model.addAttribute("customers", bankService.getCustomers(keyword));
        model.addAttribute("keyword", keyword);
        model.addAttribute("totalBalance", bankService.getTotalBalance());
        model.addAttribute("customerCount", bankService.countCustomers());
        model.addAttribute("successCount", bankService.count(TransactionStatus.SUCCESS));
        model.addAttribute("failedCount", bankService.count(TransactionStatus.FAILED));
        model.addAttribute("transactions", bankService.latestTransactions());
    }
}