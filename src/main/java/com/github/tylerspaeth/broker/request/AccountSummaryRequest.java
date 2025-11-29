package com.github.tylerspaeth.broker.request;

import com.ib.controller.AccountSummaryTag;

import java.util.List;

public record AccountSummaryRequest(List<AccountSummaryTag> accountSummaryTags) {}
