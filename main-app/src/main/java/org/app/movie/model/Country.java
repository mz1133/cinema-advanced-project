package org.app.movie.model;

import lombok.AllArgsConstructor;

import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Country {

    USA("USA"),
    UNITED_KINGDOM("United Kingdom"),
    CANADA("Canada"),
    AUSTRALIA("Australia"),
    GERMANY("Germany"),
    FRANCE("France"),
    ITALY("Italy"),
    SPAIN("Spain"),
    JAPAN("Japan"),
    SOUTH_KOREA("South Korea"),
    CHINA("China"),
    INDIA("India"),
    BRAZIL("Brazil"),
    MEXICO("Mexico"),
    RUSSIA("Russia"),
    POLAND("Poland"),
    NETHERLANDS("Netherlands"),
    BELGIUM("Belgium"),
    SWEDEN("Sweden"),
    NORWAY("Norway"),
    DENMARK("Denmark"),
    FINLAND("Finland"),
    SWITZERLAND("Switzerland"),
    AUSTRIA("Austria"),
    PORTUGAL("Portugal"),
    IRELAND("Ireland"),
    NEW_ZEALAND("New Zealand"),
    ARGENTINA("Argentina"),
    CHILE("Chile"),
    COLOMBIA("Colombia"),
    TURKEY("Turkey"),
    GREECE("Greece"),
    ISRAEL("Israel"),
    SOUTH_AFRICA("South Africa"),
    EGYPT("Egypt"),
    THAILAND("Thailand"),
    INDONESIA("Indonesia"),
    PHILIPPINES("Philippines"),
    VIETNAM("Vietnam"),
    MALAYSIA("Malaysia"),
    SINGAPORE("Singapore"),
    HONG_KONG("Hong Kong"),
    TAIWAN("Taiwan"),
    CZECH_REPUBLIC("Czech Republic"),
    HUNGARY("Hungary"),
    ROMANIA("Romania"),
    BULGARIA("Bulgaria"),
    UKRAINE("Ukraine"),
    CROATIA("Croatia"),

    OTHER("Other");

    private final String displayName;
}
