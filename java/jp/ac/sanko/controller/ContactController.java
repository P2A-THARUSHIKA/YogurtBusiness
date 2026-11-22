package jp.ac.sanko.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/contact")
public class ContactController {

    @Autowired
    private JavaMailSender mailSender;

    @PostMapping("/send")
    public String sendMessage(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String subject,
            @RequestParam String message,
            RedirectAttributes redirectAttributes) {

        try {
            SimpleMailMessage mail = new SimpleMailMessage();

            mail.setTo("rithuproducts3@gmail.com");
            mail.setReplyTo(email);
            mail.setSubject("Contact Form: " + subject);

            mail.setText(
                "New message from Rithu Milk Products Website\n\n" +
                "Customer Name: " + name + "\n" +
                "Customer Email: " + email + "\n\n" +
                "Message:\n" + message
            );

            mailSender.send(mail);

            redirectAttributes.addFlashAttribute(
                "successMessage",
                "Your message has been sent successfully!"
            );

            return "redirect:/contact";

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute(
                "errorMessage",
                "Message could not be sent. Please try again."
            );

            return "redirect:/contact";
        }
    }
}