package org.right_brothers.data.models;
/**
 * Wrapper class around the Order.
 **/
public class OrderItem {
    private Order order;
    private boolean isOrderComplete;

    public OrderItem(Order newOrder, boolean isOrderComplete) {
        this.order = newOrder;
        this.isOrderComplete = isOrderComplete;
    }

    public Order getOrder() {
        return order;
    }

    public boolean getIsOrderComplete() {
        return isOrderComplete;
    }

    public void setOrder(Order updatedOrder) {
        this.order = updatedOrder;
    }

    public void setIsOrderComplete(boolean completionStatus) {
        this.isOrderComplete = completionStatus;
    }
}