package org.right_brothers.data.models;
import java.util.Comparator;
/**
 * Class used for sorting based on delivery dates.
 **/
public class SortByDeliveryTime implements Comparator<OrderItem> 
{ 
    // Sorting in Ascending order of delivery time 
    public int compare(OrderItem a, OrderItem b) 
    {
        int timeParam1;
        int timeParam2;
        // Assuming orders do not arrive months in advance. Order has Date in only day and hours
        timeParam1 = (a.getOrder().getDeliveryDate().getDay()*24*60)+(a.getOrder().getDeliveryDate().getHour()*60)+(a.getOrder().getDeliveryDate().getMinute());
        timeParam2 = (b.getOrder().getDeliveryDate().getDay()*24*60)+(b.getOrder().getDeliveryDate().getHour()*60)+(b.getOrder().getDeliveryDate().getMinute());
        return timeParam1 - timeParam2; 
    } 
}