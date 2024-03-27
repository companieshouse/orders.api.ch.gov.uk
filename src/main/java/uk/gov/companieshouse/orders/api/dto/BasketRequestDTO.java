package uk.gov.companieshouse.orders.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.Gson;

import jakarta.validation.constraints.NotBlank;

public class BasketRequestDTO {

    @NotBlank(message = "item_uri may not be blank")
    @JsonProperty("item_uri")
    private String itemUri;

    public String getItemUri() {
        return itemUri;
    }

    public void setItemUri(String itemUri) {
        this.itemUri = itemUri;
    }

    @Override
    public String toString() { return new Gson().toJson(this); }
}
