package com.aequus.financial.statement.parser;

import com.aequus.financial.entity.FinancialCategory;
import com.aequus.financial.entity.FinancialType;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Map;

@Component
public class CategoryInferenceEngine {

    private static final Map<String, FinancialCategory> KEYWORD_MAPPINGS = Map.ofEntries(
            // Income
            Map.entry("salary", FinancialCategory.ACTIVE_INCOME),
            Map.entry("payroll", FinancialCategory.ACTIVE_INCOME),
            Map.entry("wages", FinancialCategory.ACTIVE_INCOME),
            Map.entry("direct dep", FinancialCategory.ACTIVE_INCOME),
            Map.entry("bonus", FinancialCategory.ACTIVE_INCOME),
            Map.entry("stipend", FinancialCategory.ACTIVE_INCOME),
            Map.entry("dividend", FinancialCategory.PASSIVE_INCOME),
            Map.entry("interest", FinancialCategory.PASSIVE_INCOME),
            Map.entry("yield", FinancialCategory.PASSIVE_INCOME),
            Map.entry("rental", FinancialCategory.PASSIVE_INCOME),
            Map.entry("royalty", FinancialCategory.PASSIVE_INCOME),

            // Food & Dining
            Map.entry("restaurant", FinancialCategory.FOOD),
            Map.entry("cafe", FinancialCategory.FOOD),
            Map.entry("starbucks", FinancialCategory.FOOD),
            Map.entry("mcdonald", FinancialCategory.FOOD),
            Map.entry("burger", FinancialCategory.FOOD),
            Map.entry("pizza", FinancialCategory.FOOD),
            Map.entry("swiggy", FinancialCategory.FOOD),
            Map.entry("zomato", FinancialCategory.FOOD),
            Map.entry("uber eats", FinancialCategory.FOOD),
            Map.entry("doordash", FinancialCategory.FOOD),
            Map.entry("grubhub", FinancialCategory.FOOD),
            Map.entry("grocery", FinancialCategory.FOOD),
            Map.entry("supermarket", FinancialCategory.FOOD),
            Map.entry("walmart", FinancialCategory.FOOD),
            Map.entry("trader joe", FinancialCategory.FOOD),
            Map.entry("whole foods", FinancialCategory.FOOD),
            Map.entry("kroger", FinancialCategory.FOOD),
            Map.entry("costco", FinancialCategory.FOOD),

            // Travel & Transport
            Map.entry("uber", FinancialCategory.TRAVEL),
            Map.entry("lyft", FinancialCategory.TRAVEL),
            Map.entry("ola", FinancialCategory.TRAVEL),
            Map.entry("grab", FinancialCategory.TRAVEL),
            Map.entry("taxi", FinancialCategory.TRAVEL),
            Map.entry("metro", FinancialCategory.TRAVEL),
            Map.entry("transit", FinancialCategory.TRAVEL),
            Map.entry("airline", FinancialCategory.TRAVEL),
            Map.entry("flight", FinancialCategory.TRAVEL),
            Map.entry("delta", FinancialCategory.TRAVEL),
            Map.entry("emirates", FinancialCategory.TRAVEL),
            Map.entry("hotel", FinancialCategory.TRAVEL),
            Map.entry("airbnb", FinancialCategory.TRAVEL),
            Map.entry("booking.com", FinancialCategory.TRAVEL),
            Map.entry("petrol", FinancialCategory.TRAVEL),
            Map.entry("fuel", FinancialCategory.TRAVEL),
            Map.entry("gas station", FinancialCategory.TRAVEL),
            Map.entry("shell", FinancialCategory.TRAVEL),
            Map.entry("chevron", FinancialCategory.TRAVEL),

            // Entertainment
            Map.entry("netflix", FinancialCategory.ENTERTAINMENT),
            Map.entry("spotify", FinancialCategory.ENTERTAINMENT),
            Map.entry("disney", FinancialCategory.ENTERTAINMENT),
            Map.entry("hulu", FinancialCategory.ENTERTAINMENT),
            Map.entry("apple music", FinancialCategory.ENTERTAINMENT),
            Map.entry("youtube", FinancialCategory.ENTERTAINMENT),
            Map.entry("steam", FinancialCategory.ENTERTAINMENT),
            Map.entry("playstation", FinancialCategory.ENTERTAINMENT),
            Map.entry("xbox", FinancialCategory.ENTERTAINMENT),
            Map.entry("cinema", FinancialCategory.ENTERTAINMENT),
            Map.entry("theatre", FinancialCategory.ENTERTAINMENT),
            Map.entry("movie", FinancialCategory.ENTERTAINMENT),
            Map.entry("amc", FinancialCategory.ENTERTAINMENT),

            // Education
            Map.entry("udemy", FinancialCategory.EDUCATION),
            Map.entry("coursera", FinancialCategory.EDUCATION),
            Map.entry("edx", FinancialCategory.EDUCATION),
            Map.entry("university", FinancialCategory.EDUCATION),
            Map.entry("college", FinancialCategory.EDUCATION),
            Map.entry("school", FinancialCategory.EDUCATION),
            Map.entry("tuition", FinancialCategory.EDUCATION),
            Map.entry("book", FinancialCategory.EDUCATION),
            Map.entry("kindle", FinancialCategory.EDUCATION),

            // Clothing & Apparel
            Map.entry("zara", FinancialCategory.CLOTHING),
            Map.entry("h&m", FinancialCategory.CLOTHING),
            Map.entry("nike", FinancialCategory.CLOTHING),
            Map.entry("adidas", FinancialCategory.CLOTHING),
            Map.entry("uniqlo", FinancialCategory.CLOTHING),
            Map.entry("apparel", FinancialCategory.CLOTHING),
            Map.entry("clothing", FinancialCategory.CLOTHING),
            Map.entry("fashion", FinancialCategory.CLOTHING)
    );

    public FinancialCategory inferCategory(String description, FinancialType type) {
        if (description == null || description.isBlank()) {
            return getDefaultCategory(type);
        }

        String normalized = description.toLowerCase(Locale.ROOT);

        for (Map.Entry<String, FinancialCategory> entry : KEYWORD_MAPPINGS.entrySet()) {
            if (normalized.contains(entry.getKey())) {
                FinancialCategory candidate = entry.getValue();
                if (candidate.getType() == type) {
                    return candidate;
                }
            }
        }

        return getDefaultCategory(type);
    }

    private FinancialCategory getDefaultCategory(FinancialType type) {
        return type == FinancialType.INCOME ? FinancialCategory.ACTIVE_INCOME : FinancialCategory.MISCELLANEOUS;
    }
}
