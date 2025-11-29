package com.github.tylerspaeth.broker.response;

import java.util.ArrayList;
import java.util.List;

public class PositionResponse {

    public String accountId;
    public final List<Position> positions = new ArrayList<>();

}
