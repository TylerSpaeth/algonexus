package com.github.tylerspaeth.common.data.entity;

import com.github.tylerspaeth.common.enums.OrderStatusEnum;
import jakarta.persistence.*;

import java.sql.Timestamp;

@Entity
@Table(name = "orderevents")
public class OrderEvent {

    @Id
    @Column(name = "OrderEventID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer orderEventID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OrderID", referencedColumnName = "OrderID", nullable = false)
    private Order order;

    @Column(name = "NewStatus")
    @Enumerated(EnumType.STRING)
    private OrderStatusEnum newStatus;

    @Column(name = "Timestamp")
    private Timestamp timestamp;

    public Integer getOrderEventID() {
        return orderEventID;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public OrderStatusEnum getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(OrderStatusEnum newStatus) {
        this.newStatus = newStatus;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
