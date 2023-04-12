package net.CA;

import java.io.IOException;
import java.security.Principal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AppController {

	@Autowired
	private UserRepository userRepo;

	@Autowired
	private RoleRepository roleRepo;

	@Autowired
	private ItemRepository itemRepository;
	@Autowired
	private ItemService itemServiceImp;
	
	@GetMapping("/index")
	public String viewHomePage() {
		return "index";
	}
	
	@GetMapping("/register")
	public String showRegistrationForm(Model model) {
		model.addAttribute("user", new User());
		
		return "signup_form";
	}
	
	@PostMapping("/process_register")
	public String processRegister(User user) {

		Role customerRole = roleRepo.findByName("CUSTOMER");
		user.getRoles().add(customerRole);
		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		String encodedPassword = passwordEncoder.encode(user.getPassword());
		user.setPassword(encodedPassword);

		userRepo.save(user);
		
		return "register_success";
	}
	
	@GetMapping("/users")
	public String listUsers(Model model) {
		List<User> listUsers = userRepo.findAll();
		model.addAttribute("listUsers", listUsers);
		
		return "users";
	}

	@PreAuthorize("hasRole('CUSTOMER')")
	@GetMapping("/home_page")
	public String homePage(Model model,
						   @RequestParam(name = "sort", defaultValue = "lowest") String sort,
						   @RequestParam(name = "category", required = false) String category,
						   @Param("keyword") String keyword) {
		List<Item> itemsList = (List<Item>) itemServiceImp.itemsList(keyword);
		//chain of responsibility Pattern
		if (category != null) {
			itemsList = itemsList.stream()
					.filter(item -> item.getCategory().equalsIgnoreCase(category))
					.collect(Collectors.toList());
		}
		//Strategy Pattern
		if (sort.equals("lowest")) {
			itemsList.sort(Comparator.comparing(Item::getPrice));
		} else if (sort.equals("highest")) {
			itemsList.sort(Comparator.comparing(Item::getPrice).reversed());
		}
		model.addAttribute("listItems", itemsList);
		return "home_page";
	}

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/admin_home")
	public String adminHome() {
		return "admin_home";
	}

	@GetMapping("/listProduct")
	public String listing(Model model) {
		model.addAttribute("item", new Item());
		model.addAttribute("pageTitle", "Sell Product");
		return "addProduct";
	}

	@GetMapping("/product")
	public String viewHome(Model model, @Param("keyword") String keyword) {
		List<Item> itemsList = (List<Item>) itemServiceImp.itemsList(keyword);
		model.addAttribute("listItems", itemsList);

		return "listStock";
	}

	@PostMapping("/product/save")
	public String itemAdd(Item item) throws IOException {
		itemRepository.save(item);
		return "redirect:/product";
	}

	@GetMapping("/product/update/{itemId}")
	public String updateItem(@PathVariable("itemId") Long itemId, Model model) {
		try {
			Item item = itemServiceImp.get(itemId);
			model.addAttribute("item", item);
			return "addProduct";
		} catch (ItemNotFoundException e) {

			return "redirect:/product";
		}

	}

	@GetMapping("/product/delete/{itemId}")
	public String deleteItem(@PathVariable("itemId") Long itemId) {
		itemServiceImp.delete(itemId);
		return "redirect:/product";
	}


}
