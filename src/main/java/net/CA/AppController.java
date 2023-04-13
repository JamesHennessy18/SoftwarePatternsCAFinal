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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

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

	@Autowired
	private OrderRepository orderRepository;
	
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

	private Cart getCart(HttpServletRequest request) {
		HttpSession session = request.getSession();
		Cart cart = (Cart) session.getAttribute("cart");
		if (cart == null) {
			cart = new Cart();
			session.setAttribute("cart", cart);
		}
		return cart;
	}

	@PostMapping("/add_to_cart")
	public String addToCart(@RequestParam("itemId") Long itemId,
							@RequestParam("quantity") int quantity,
							HttpServletRequest request,
							RedirectAttributes redirectAttributes) {
		Item item = itemRepository.findByItemId(itemId);
		int availableStock = item.getStock();

		// Get the Cart object from the user's session (or create a new one)
		HttpSession session = request.getSession();
		Cart cart = (Cart) session.getAttribute("cart");
		if (cart == null) {
			cart = new Cart();
			session.setAttribute("cart", cart);
		}

		if (quantity > availableStock) {
			// If the user tries to add more items than the available stock,
			// you can show an error message or redirect them to an error page.
			redirectAttributes.addFlashAttribute("errorMessage", "Cannot add more items than available stock.");
			return "redirect:/home_page";
		} else {
			// Check if the item already exists in the cart
			CartItem existingCartItem = cart.findItemById(itemId);

			if (existingCartItem != null) {
				// If the item already exists, update its quantity
				int newQuantity = existingCartItem.getQuantity() + quantity;
				if (newQuantity <= availableStock) {
					existingCartItem.setQuantity(newQuantity);
				} else {
					// If the updated quantity exceeds the available stock, show an error message
					redirectAttributes.addFlashAttribute("errorMessage", "Cannot add more items than available stock.");
					return "redirect:/home_page";
				}
			} else {
				// If the item doesn't exist in the cart, add it with the requested quantity
				cart.addItem(item, quantity);
			}
		}

		return "redirect:/home_page";
	}

	@GetMapping("/cart")
	public String showCart(Model model, Principal principal, HttpServletRequest request) {
		Cart cart = getCart(request);
		User user = userRepo.findByEmail(principal.getName());
		model.addAttribute("cart", cart);
		model.addAttribute("user", user);
		return "cart";
	}
	@GetMapping("/order_success")
	public String showOrderSuccess() {
		return "order_success";
	}

	@PostMapping("/place_order")
	public String placeOrder(@ModelAttribute("shippingAddress") String shippingAddress,
							 HttpServletRequest request,
							 RedirectAttributes redirectAttributes,
							 Principal principal) {

		User user = userRepo.findByEmail(principal.getName());

		HttpSession session = request.getSession();
		Cart cart = (Cart) session.getAttribute("cart");

		String productNames = cart.getItems().stream()
				.map(cartItem -> cartItem.getItem().getTitle())
				.collect(Collectors.joining(", "));
		OrderComplete orderComplete = new OrderComplete(user, productNames, cart.getTotal(), shippingAddress);
		orderRepository.save(orderComplete);
		session.removeAttribute("cart");
		redirectAttributes.addFlashAttribute("orderComplete", orderComplete);
		return "redirect:/order_success";
	}
}
