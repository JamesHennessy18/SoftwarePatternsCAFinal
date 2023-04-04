package net.CA;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ItemService {
    @Autowired
    ItemRepository itemRepository;

    public List<Item> itemsList(String keyword) {
       if (keyword != null) {
            return itemRepository.search(keyword);
        }
        return (List<Item>) itemRepository.findAll();
    }

    public Item get(Long itemId) throws ItemNotFoundException {
        Optional<Item> result = itemRepository.findById(itemId);

        if (result.isPresent()) {
            return result.get();
        }

        throw new ItemNotFoundException("No Item with id: " + itemId);
    }

    public void delete(Long itemId) {
        itemRepository.deleteById(itemId);
    }
}
