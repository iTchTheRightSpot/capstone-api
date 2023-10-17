package com.sarabrandserver.stripe;

import com.sarabrandserver.enumeration.SarreCurrency;

public record PriceCurrencyPair (long unitAmount, SarreCurrency currency) { }