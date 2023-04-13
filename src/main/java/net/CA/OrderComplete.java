package net.CA;

import javax.persistence.*;
import javax.persistence.Table;


    @Entity
    @Table(name = "OrderComplete")
    public class OrderComplete {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne
        @JoinColumn(name = "user_id")
        private User user;

        @Column(nullable = false)
        private String productNames;

        @Column(nullable = false)
        private Double total;

        @Column(name = "shipping_address", nullable = false)
        private String shippingAddress;

        public OrderComplete(User user, String productNames, Double total, String shippingAddress) {
            this.user = user;
            this.productNames = productNames;
            this.total = total;
            this.shippingAddress = shippingAddress;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }

        public String getProductNames() {
            return productNames;
        }

        public void setProductNames(String productNames) {
            this.productNames = productNames;
        }

        public Double getTotal() {
            return total;
        }

        public void setTotal(Double total) {
            this.total = total;
        }

        public String getShippingAddress() {
            return shippingAddress;
        }

        public void setShippingAddress(String shippingAddress) {
            this.shippingAddress = shippingAddress;
        }
    }

