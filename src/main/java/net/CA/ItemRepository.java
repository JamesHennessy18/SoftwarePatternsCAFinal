package net.CA;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends CrudRepository<Item, Long> {
    @Query("SELECT i FROM Item i WHERE i.itemId = ?1")
    Item findByItemId(Long itemId);

    @Query("SELECT i FROM Item i WHERE CONCAT(i.title, i.category, i.manufacturer) LIKE %?1%")
    public List<Item> search(String keyword);

}