package com.github.tylerspaeth.broker.response;

import java.sql.Timestamp;

public record OrderStatus(com.ib.client.OrderStatus orderStatus, Timestamp timestamp) {

}
