from pydantic import BaseModel, Field
from typing import List

# Corresponds to OptimizationLimitsDto.java (Input DTO)
class OptimizationLimits(BaseModel):
    """Define os limites de temperatura e conforto para o Agente de otimização."""
    
    max_temp: float = Field(alias="maxTemp")
    min_comfort_temp: float = Field(alias="minComfortTemp")
    
    class Config:
        populate_by_name = True 

# Corresponds to OptimizationRequest.java (Input DTO)
class OptimizationRequest(BaseModel):
    """Recebe dados de consumo histórico e previsão do tempo do Gateway Java."""
    
    historical_consumption_kwh: List[float] = Field(alias="historicalConsumptionKwH")
    weather_forecast_degrees_c: List[float] = Field(alias="weatherForecastDegreesC")
    limits: OptimizationLimits
    
    # CORREÇÃO FINAL: Campo 'selected_language' adicionado, esperado como string (ex: 'en', 'pt').
    selected_language: str = Field(alias="selectedLanguage")
    
    class Config:
        populate_by_name = True

# Corresponds to OptimizationRecommendation.java (Output DTO)
class OptimizationRecommendation(BaseModel):
    """Retorna a recomendação final, incluindo a justificativa do Gemini."""
    
    actionable_script: str = Field(alias="actionableScript")
    natural_language_justification: str = Field(alias="naturalLanguageJustification")
    estimated_cost_savings_eur: float = Field(alias="estimatedCostSavingsEur")
    estimated_carbon_footprint_reduction_kg_co2: float = Field(alias="estimatedCarbonFootprintReductionKgCO2")
    recommendation_id: str = Field(alias="recommendationId")
    
    class Config:
        by_alias = True
        populate_by_name = True