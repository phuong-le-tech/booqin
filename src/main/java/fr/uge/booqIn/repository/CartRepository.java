package fr.uge.booqIn.repository;

import fr.uge.booqIn.dto.user.UserShow;
import fr.uge.booqIn.entity.Cart;
import fr.uge.booqIn.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    Cart findByUser(User user);
    Optional<Cart> findByPaymentIntentId(String paymentIntentId);

  
}