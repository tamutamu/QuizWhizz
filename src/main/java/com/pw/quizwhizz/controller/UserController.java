package com.pw.quizwhizz.controller;

import com.pw.quizwhizz.model.account.User;
import com.pw.quizwhizz.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;


@Controller
public class UserController {

	private UserService userService;
	
	@Autowired
	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	@GetMapping("/loginform")
	public String loginForm() {
		return "login_form";
	}

	@GetMapping("/loginerror")
	public String loginError(Model model) {
		model.addAttribute("loginError", true);
		return "login_form";
	}

	@GetMapping("/register")
	public String register(Model model) {
		model.addAttribute("user", new User());
		return "register_form";
	}

	@PostMapping("/register")
	public String addUser(@ModelAttribute @Valid User user, BindingResult bindResult, HttpServletRequest request) throws ServletException {
		if(bindResult.hasErrors())
			return "register_form";
		else {
			userService.addWithDefaultRole(user);
			request.login(user.getEmail(),user.getPassword()); // auto logowanie

			return "register_success";
		}
	}

	@GetMapping("/user/{userId}")
	public String userProfile(
			@PathVariable Long userId,
			Authentication authentication,
			Model model) {
		User user = userService.findById(userId);
		boolean userCheckHimself = false;
		if(user.getEmail().equals(authentication.getName())){
			userCheckHimself = true; // czy user sprawdza swoj profil (dodaje opcje edytowania)
		}
		model.addAttribute("userCheckHimself", userCheckHimself);
		model.addAttribute("user", user);
		return "user_profile";
	}

	@GetMapping("/user/my")
	public String userProfileMy(
			Authentication authentication,
			Model model) {
		boolean userCheckHimself = true;
		model.addAttribute("userCheckHimself", userCheckHimself);
		model.addAttribute("user", userService.findByEmail(authentication.getName()));
		return "user_profile";
	}

	@PostMapping("/user/editme")
	public String postUserEditMe(
			@ModelAttribute User user,
			@RequestParam MultipartFile file,
			BindingResult bindResult,
			HttpServletRequest request) {
		String saveDirectory = request.getSession().getServletContext().getRealPath("/")+"resources\\images\\";

		if (bindResult.hasErrors())
			//TODO HTTP 400
			return "redirect:/user/my";
		else {
			if (!file.isEmpty()) {
				try {
					userService.updateUserWithImage(user, file, saveDirectory);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			else{
				userService.update(user);
			}
			return "redirect:/user/my";
		}
	}

}