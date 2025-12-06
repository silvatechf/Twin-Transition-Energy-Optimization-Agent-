export interface OptimizationLimits {
  maxTemp: number;
  minComfortTemp: number;
}

export interface OptimizationRequest {
  historicalConsumptionKwH: number[];
  weatherForecastDegreesC: number[];
  limits: OptimizationLimits;
  // CORREÇÃO TS2322: Adicionado o campo que o componente está enviando
  selectedLanguage: string; 
}

export interface OptimizationRecommendation {
  actionableScript: string;
  naturalLanguageJustification: string;
  estimatedCostSavingsEur: number;
  estimatedCarbonFootprintReductionKgCO2: number;
  recommendationId: string;
}

export interface ApiResponse<T> {
  message: string;
  data: T;
  status: number;
}