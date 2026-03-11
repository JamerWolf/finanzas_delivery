package com.control_delivery.finanzas_delivery.data.json

import com.control_delivery.finanzas_delivery.domain.model.DistanceExpenseType
import com.control_delivery.finanzas_delivery.domain.model.ExpenseFrequency
import com.google.gson.*
import java.lang.reflect.Type

class DistanceExpenseTypeAdapter : JsonSerializer<DistanceExpenseType>, JsonDeserializer<DistanceExpenseType> {
    override fun serialize(src: DistanceExpenseType, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        val jsonObject = context.serialize(src).asJsonObject
        jsonObject.addProperty("type", src.javaClass.simpleName)
        return jsonObject
    }

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): DistanceExpenseType {
        val jsonObject = json.asJsonObject
        val typeElement = jsonObject.get("type")
        
        val type = if (typeElement != null && !typeElement.isJsonNull) {
            typeElement.asString
        } else {
            // Inference for legacy data
            if (jsonObject.has("pricePerUnit")) "PureDeduction"
            else if (jsonObject.has("targetAmount")) "SavingsGoal"
            else throw JsonParseException("Cannot infer DistanceExpenseType from JSON: $json")
        }

        return when (type) {
            "PureDeduction" -> context.deserialize(json, DistanceExpenseType.PureDeduction::class.java)
            "SavingsGoal" -> context.deserialize(json, DistanceExpenseType.SavingsGoal::class.java)
            else -> throw JsonParseException("Unknown DistanceExpenseType: $type")
        }
    }
}

class ExpenseFrequencyAdapter : JsonSerializer<ExpenseFrequency>, JsonDeserializer<ExpenseFrequency> {
    override fun serialize(src: ExpenseFrequency, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        val jsonObject = if (src is ExpenseFrequency.Daily) JsonObject() else context.serialize(src).asJsonObject
        jsonObject.addProperty("type", src.javaClass.simpleName)
        return jsonObject
    }

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): ExpenseFrequency {
        val jsonObject = json.asJsonObject
        val typeElement = jsonObject.get("type")

        val type = if (typeElement != null && !typeElement.isJsonNull) {
            typeElement.asString
        } else {
            // Inference for legacy data
            if (jsonObject.has("dayOfWeek")) "Weekly"
            else if (jsonObject.has("dayOfMonth") && jsonObject.has("month")) "Yearly"
            else if (jsonObject.has("dayOfMonth")) "Monthly"
            else if (jsonObject.has("timestamp")) "Once"
            else "Daily" // Default fallback
        }

        return when (type) {
            "Daily" -> ExpenseFrequency.Daily
            "Weekly" -> context.deserialize(json, ExpenseFrequency.Weekly::class.java)
            "Monthly" -> context.deserialize(json, ExpenseFrequency.Monthly::class.java)
            "Yearly" -> context.deserialize(json, ExpenseFrequency.Yearly::class.java)
            "Once" -> context.deserialize(json, ExpenseFrequency.Once::class.java)
            else -> throw JsonParseException("Unknown ExpenseFrequency: $type")
        }
    }
}

object GsonFactory {
    val gson: Gson = GsonBuilder()
        .registerTypeAdapter(DistanceExpenseType::class.java, DistanceExpenseTypeAdapter())
        .registerTypeAdapter(ExpenseFrequency::class.java, ExpenseFrequencyAdapter())
        .create()
}
