package com.example.template.order;

import com.example.template.Application;
import com.example.template.delivery.Delivery;
import com.example.template.delivery.DeliveryService;
import com.example.template.delivery.DeliveryStatus;
import com.example.template.product.Product;
import com.example.template.product.ProductRepository;
import com.example.template.product.ProductService;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.RestTemplate;

import javax.persistence.*;
import java.util.Optional;

@Entity
@Table(name = "order_table")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long productId;
    private String productName;
    private int quantity;
    private int price;
    private String customerId;
    private String customerName;
    private String customerAddr;
    private String state = "OrderPlaced";

    @ManyToOne
    @JoinColumn(name = "product_idx", nullable = false, updatable = false)
    private Product product;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "order")
    @PrimaryKeyJoinColumn
    private Delivery delivery;

    @PrePersist
    private void orderCheck(){
        if( productId == null ){
            throw new RuntimeException();
        }

        int price = 0;
        String productName = null;

        ProductRepository productRepository = Application.applicationContext.getBean(ProductRepository.class);
        Optional<Product> productOptional = productRepository.findById(productId);
        Product product = productOptional.get();

        price = product.getPrice();
        productName = product.getName();
        if( product.getStock() < getQuantity()){
            throw new OrderException("No Available stock!");
        }

        this.setPrice(price);
        this.setProductName(productName);
        this.setProduct(product);
    }

    /**
     * 주문이 들어옴
     */
    @PostPersist
    private void publishOrderPlaced(){
        // 상품 정보 변경
        ProductService productService = Application.applicationContext.getBean(ProductService.class);
        productService.onOrderPlace(this);

        // 배송 정보 변경
        DeliveryService deliveryService = Application.applicationContext.getBean(DeliveryService.class);
        deliveryService.onDeliveryStart(this);
    }

    @PreUpdate
    private void publishOrderCancelled(){
        /**
         * 주문이 취소됨
         */
        if( "OrderCancelled".equals(this.getState())){
            System.out.println("this.getState() = " + this.getState());
            // 상품 정보 변경
            ProductService productService = Application.applicationContext.getBean(ProductService.class);
            productService.onOrderCancel(this);

            // 배송 정보 변경
            DeliveryService deliveryService = Application.applicationContext.getBean(DeliveryService.class);
            deliveryService.onOrderCancel(this);
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerAddr() {
        return customerAddr;
    }

    public void setCustomerAddr(String customerAddr) {
        this.customerAddr = customerAddr;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Delivery getDelivery() {
        return delivery;
    }

    public void setDelivery(Delivery delivery) {
        this.delivery = delivery;
    }
}