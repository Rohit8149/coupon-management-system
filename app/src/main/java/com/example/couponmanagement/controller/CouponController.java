package com.example.couponmanagement.controller;

import com.example.couponmanagement.model.Coupon;
import com.example.couponmanagement.service.CouponService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CouponController {

    private final CouponService couponService;

    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    @GetMapping("/")
    public String index(Model model) {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("metrics", couponService.getDashboardMetrics());
        model.addAttribute("recentCoupons", couponService.getAllCoupons());
        return "dashboard";
    }

    @GetMapping("/coupons")
    public String listCoupons(@RequestParam(value = "query", required = false) String query,
                              @RequestParam(value = "status", required = false) String status,
                              Model model) {
        model.addAttribute("coupons", couponService.searchCoupons(query, status));
        model.addAttribute("query", query != null ? query : "");
        model.addAttribute("status", status != null ? status : "ALL");
        return "coupons";
    }

    @GetMapping("/coupons/create")
    public String showCreateForm(Model model) {
        if (!model.containsAttribute("coupon")) {
            model.addAttribute("coupon", new Coupon());
        }
        return "create";
    }

    @PostMapping("/coupons/create")
    public String createCoupon(@Valid @ModelAttribute("coupon") Coupon coupon,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "create";
        }
        try {
            couponService.createCoupon(coupon);
            redirectAttributes.addFlashAttribute("successMessage", "Coupon '" + coupon.getCode() + "' created successfully!");
            return "redirect:/coupons";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            redirectAttributes.addFlashAttribute("coupon", coupon);
            return "redirect:/coupons/create";
        }
    }

    @PostMapping("/coupons/{id}/status")
    public String updateStatus(@PathVariable("id") Long id,
                               @RequestParam("status") String targetStatus,
                               RedirectAttributes redirectAttributes) {
        try {
            Coupon updated = couponService.updateStatus(id, targetStatus);
            redirectAttributes.addFlashAttribute("successMessage", "Coupon '" + updated.getCode() + "' updated to " + targetStatus + "!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/coupons";
    }

    @PostMapping("/coupons/{id}/redeem")
    public String redeemCoupon(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            Coupon updated = couponService.redeemCoupon(id);
            redirectAttributes.addFlashAttribute("successMessage", "Coupon '" + updated.getCode() + "' redeemed successfully!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/coupons";
    }
}
