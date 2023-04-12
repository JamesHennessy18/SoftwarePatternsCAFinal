package net.CA;

import java.util.ArrayList;
import java.util.List;

public class Cart {
    private List<CartItem> items;

    public Cart() {
        items = new ArrayList<>();
    }

    public void addItem(Item item, int quantity) {
        // Check if item is already in the cart
        for (CartItem cartItem : items) {
            if (cartItem.getItem().getItemId() == (item.getItemId())) {
                cartItem.setQuantity(cartItem.getQuantity() + quantity);
                return;
            }
        }
        items.add(new CartItem(item, quantity));
    }

    public double getTotal() {
        return items.stream()
                .mapToDouble(cartItem -> cartItem.getItem().getPrice() * cartItem.getQuantity())
                .sum();
    }
    public List<CartItem> getItems() {
        return items;
    }
    public CartItem findItemById(Long itemId) {
        for (CartItem cartItem : items) {
            if (cartItem.getItem().getItemId() == (itemId)) {
                return cartItem;
            }
        }
        return null;
    }

}
