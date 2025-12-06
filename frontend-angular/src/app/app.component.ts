import { Component, inject, signal, computed, ChangeDetectionStrategy } from '@angular/core';
import { HttpClientModule } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms'; 
import { timer, tap } from 'rxjs'; 

import { EnergyService } from './services/energy.service'; 
import { OptimizationRequest, OptimizationRecommendation, ApiResponse } from './models/optimization.model';

// Definição do Componente Standalone
@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule, HttpClientModule],
  template: `
    <div class="min-h-screen bg-gray-100 flex flex-col items-center font-sans">
      
      <!-- Seção de Cabeçalho (Blue Banner GOV.UK) -->
      <header class="w-full bg-[#005ea5] text-white py-8 px-4 sm:px-8 shadow-xl">
        <div class="max-w-5xl mx-auto">
          <!-- Título Principal (Fonte Grande e Ousada) -->
          <h1 class="text-4xl sm:text-6xl font-extrabold tracking-tight mb-2">
            {{ T('mainTitle') }}
          </h1>
          <p class="text-lg opacity-90">
            {{ T('headerSubtitle') }}
          </p>
        </div>
      </header>

      <!-- Layout Principal (Conteúdo Central Alinhado) -->
      <main class="w-full max-w-5xl p-4 sm:p-6 lg:p-8 space-y-8">
        
        <!-- Bloco de Dicas/Contexto (UX GOV.UK Style) -->
        <div class="p-4 bg-yellow-50 border-l-4 border-yellow-600 shadow-sm rounded">
          <p class="text-sm text-gray-700 font-semibold">{{ T('uxTip') }}</p>
        </div>

        <!-- Layout de Três Colunas (Input e Output) -->
        <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
          
          <!-- Coluna de Input (2/3 em Desktop) -->
          <section class="lg:col-span-2 bg-white p-6 rounded-lg shadow-md border border-gray-300">
            <h2 class="text-2xl font-bold text-[#005ea5] mb-6 border-b-2 border-gray-200 pb-2">{{ T('paramsTitle') }}</h2>
            
            <div class="space-y-6">
              
              <!-- Simulação de Consumo -->
              <div class="flex flex-col space-y-1">
                <label class="block text-sm font-semibold text-gray-700">{{ T('simulatedConsumptionLabel') }}</label>
                <p class="text-xs text-gray-500 mb-1">{{ T('simulatedConsumptionHint') }}</p>
                <input 
                  [(ngModel)]="consumptionInput"
                  type="text" 
                  placeholder="Ex: 100.0, 110.0, 120.0" 
                  class="w-full p-2 border-2 border-gray-400 rounded-sm focus:border-blue-600 focus:ring-transparent"
                />
              </div>

              <!-- Previsão do Tempo -->
              <div class="flex flex-col space-y-1">
                <label class="block text-sm font-semibold text-gray-700">{{ T('weatherForecastLabel') }}</label>
                <p class="text-xs text-gray-500 mb-1">{{ T('weatherForecastHint') }}</p>
                <input 
                  [(ngModel)]="weatherInput"
                  type="text" 
                  placeholder="Ex: 25.0, 26.0, 28.0" 
                  class="w-full p-2 border-2 border-gray-400 rounded-sm focus:border-blue-600 focus:ring-transparent"
                />
              </div>

              <!-- Limites de Conforto -->
              <div class="grid grid-cols-1 sm:grid-cols-2 gap-4 pt-4 border-t border-gray-200">
                <div>
                  <label class="block text-sm font-semibold text-gray-700">{{ T('maxTempLabel') }}</label>
                  <input 
                    [(ngModel)]="maxTemp"
                    type="number" 
                    class="w-full p-2 border-2 border-gray-400 rounded-sm"
                    placeholder="24.0"
                  />
                </div>
                <div>
                  <label class="block text-sm font-semibold text-gray-700">{{ T('minTempLabel') }}</label>
                  <input 
                    [(ngModel)]="minTemp"
                    type="number" 
                    class="w-full p-2 border-2 border-gray-400 rounded-sm"
                    placeholder="20.0"
                  />
                </div>
              </div>

              <!-- Seletor de Idioma -->
              <div class="flex items-center gap-4 pt-4 border-t border-gray-200">
                <label class="text-sm font-semibold text-gray-700 whitespace-nowrap">{{ T('messageLanguageLabel') }}:</label>
                <select 
                  [(ngModel)]="selectedLanguage"
                  class="p-2 border-2 border-gray-400 rounded-sm"
                >
                  <option value="en">English (Default)</option>
                  <option value="es">Español</option>
                  <option value="pt">Português</option>
                </select>
              </div>
              
              <!-- Botão de Ação (Alto Contraste) -->
              <button
                (click)="runAgent()"
                [disabled]="isLoading()"
                class="w-full px-4 py-3 bg-[#00703c] text-white font-extrabold text-lg rounded-sm shadow-md hover:bg-[#005e32] transition duration-150 disabled:bg-gray-400 disabled:shadow-none mt-6"
              >
                {{ isLoading() ? T('runningAgent') : T('runOptimizationAgent') }}
              </button>
              
            </div>
          </section>

          <!-- Coluna de Output (Resultados) -->
          <section class="lg:col-span-1 bg-white p-6 rounded-lg shadow-md border-l-4 border-green-600">
            <h2 class="text-2xl font-bold text-gray-700 mb-6 border-b-2 border-gray-200 pb-2">{{ T('agentRecommendationTitle') }}</h2>
            
            <div *ngIf="isLoading()" class="text-center p-8">
              <div class="animate-spin rounded-full h-8 w-8 border-b-4 border-blue-600 mx-auto"></div>
              <p class="mt-4 text-gray-600 font-semibold">{{ T('awaitingGemini') }}</p>
              <p class="text-sm text-gray-500 animate-pulse">{{ loadingMessage() }}</p>
            </div>

            <div *ngIf="!isLoading()">
              
              <!-- Mensagem de Sucesso (Estilo GOV.UK - Verde/Azul Suave) -->
              <div *ngIf="successMessage()" class="p-3 mb-4 bg-green-50 text-green-800 rounded-md font-semibold border border-green-300">
                  {{ successMessage() }}
              </div>
              
              <div *ngIf="agentRecommendation()" class="space-y-4">
                
                <!-- Cartão de Economia -->
                <div class="p-4 bg-green-100 rounded-md shadow-inner border border-green-400">
                  <p class="text-base font-bold text-green-800">{{ T('estimatedSavingsFinancial') }}</p>
                  <p class="text-4xl font-black text-green-700">{{ formattedSavings() }} €</p>
                  
                  <hr class="my-3 border-green-400"/>
                  
                  <p class="text-base font-bold text-green-800 mt-2">{{ T('co2ReductionSustainability') }}</p>
                  <p class="text-xl font-bold text-green-700">{{ agentRecommendation()!.estimatedCarbonFootprintReductionKgCO2 | number:'1.2-2' }} kg CO2</p>
                </div>

                <!-- Justificativa (Gemini Output) -->
                <div class="border-t pt-4">
                  <p class="text-base font-bold text-gray-700 mb-2">{{ T('prescriptiveJustificationLabel') }}:</p>
                  <p class="text-gray-800 italic bg-gray-50 p-3 rounded-sm border border-gray-300">{{ agentRecommendation()!.naturalLanguageJustification }}</p>
                </div>

                <!-- Script de Ação -->
                <div class="border-t pt-4">
                  <p class="text-base font-bold text-gray-700 mb-2">{{ T('actionableScriptLabel') }}:</p>
                  <code class="block bg-gray-800 text-yellow-300 p-2 rounded-sm text-xs break-words shadow-inner font-mono">
                    {{ agentRecommendation()!.actionableScript }}
                  </code>
                </div>
                
                <!-- Botão de Aprovação (Simulado) -->
                <button
                  class="w-full mt-4 px-4 py-3 bg-[#005ea5] text-white font-bold text-lg rounded-sm shadow-lg hover:bg-[#004e93] transition duration-150"
                >
                  {{ T('approveAndAutomate') }}
                </button>

              </div>
              
              <div *ngIf="!agentRecommendation() && !errorMessage()">
                 <p class="text-gray-500 text-center p-4">{{ T('initialMessage') }}</p>
              </div>
              
              <!-- Mensagem de Erro (Estilo GOV.UK - Vermelho/Alerta) -->
              <div *ngIf="errorMessage()" class="text-red-700 bg-red-100 p-3 rounded-md mt-4 font-semibold border border-red-500">
                <p class="font-bold">{{ T('errorLabel') }}:</p>
                <p>{{ errorMessage() }}</p>
              </div>
            </div>
            
          </section>
        </div>
      </main>

    </div>
  `,
  styles: [`
    /* Adicionando alguns estilos para simular a tipografia GOV.UK mais robusta */
    /* Usando font-sans como fallback para a fonte customizada GDS Transport */
    h1, h2, label, button {
      font-weight: 700; /* Extra bold para impacto */
    }
    .text-4xl {
        line-height: 1.1; /* Títulos mais compactos e impactantes */
    }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush, 
})
export class AppComponent {
  private energyService = inject(EnergyService);
  
  consumptionInput: string = '100.0, 110.0, 120.0'; 
  weatherInput: string = '25.0, 26.0, 28.0'; 
  maxTemp: number = 24.0;
  minTemp: number = 20.0;
  selectedLanguage: string = 'pt'; 

  isLoading = signal(false);
  errorMessage = signal<string | null>(null);
  successMessage = signal<string | null>(null); 
  agentRecommendation = signal<OptimizationRecommendation | null>(null);
  
  // Lista de mensagens de loading para simulação de UX
  loadingMessages = [
    "Analyzing consumption and weather correlations...",
    "Computing optimal HVAC adjustments...",
    "Generating prescriptive justification via LLM...",
  ];
  loadingMessage = signal(this.loadingMessages[0]);
  
  // Mapeamento de idioma
  private translations: any = {
    // ... (Mapa de traduções, incluindo a chave 'mainTitle' e 'uxTip')
    en: {
      mainTitle: 'Twin Transition Energy Optimization Agent',
      headerSubtitle: 'Prescriptive Analysis and Carbon Footprint Reduction for European SMEs.',
      uxTip: 'Using energy consumption patterns + temperature forecast, the agent computes optimal HVAC adjustments to minimize cost and CO₂ without compromising comfort.',
      paramsTitle: 'Optimization Parameters',
      simulatedConsumptionLabel: 'Simulated Consumption (kWh, last 3 hours)',
      simulatedConsumptionHint: 'Simple simulation of historical data for the MVP.',
      weatherForecastLabel: 'Weather Forecast (ºC, next 3 hours)',
      weatherForecastHint: 'Simulation of weather forecast for correlation.',
      maxTempLabel: 'Max Comfort Temp (ºC)',
      minTempLabel: 'Min Comfort Temp (ºC)',
      messageLanguageLabel: 'Message Language',
      runOptimizationAgent: 'Run Optimization Agent',
      runningAgent: 'Running Agent...',
      agentRecommendationTitle: 'Agent Recommendation',
      awaitingGemini: 'Awaiting Gemini Agent...',
      estimatedSavingsFinancial: 'Estimated Savings (Financial)',
      co2ReductionSustainability: 'CO2 Reduction (Sustainability)',
      prescriptiveJustificationLabel: 'Prescriptive Justification',
      actionableScriptLabel: 'Actionable Script (IoT Command)',
      approveAndAutomate: 'Approve & Automate (Simulated)',
      initialMessage: 'Enter parameters and run the agent to see recommendations.',
      errorLabel: 'Error',
      validationInputFormatError: 'Invalid input format. Please use comma-separated numbers (e.g., 100.0, 110.0).',
      validationMaxMinError: 'Max temperature must be greater than min comfort temperature.'
    },
    pt: {
      mainTitle: 'Agente de Otimização de Energia Twin Transition',
      headerSubtitle: 'Análise Prescritiva e Redução de Pegada de Carbono para PMEs Europeias.',
      uxTip: 'Usando padrões de consumo de energia + previsão do tempo, o agente calcula ajustes ideais de HVAC para minimizar custos e CO₂ sem comprometer o conforto.',
      paramsTitle: 'Parâmetros de Otimização',
      simulatedConsumptionLabel: 'Consumo Simulado (kWh, últimas 3 horas)',
      simulatedConsumptionHint: 'Simulação simples de dados históricos para o MVP.',
      weatherForecastLabel: 'Previsão do Tempo (ºC, próximas 3 horas)',
      weatherForecastHint: 'Simulação de previsão do tempo para correlação.',
      maxTempLabel: 'Temp Máxima de Conforto (ºC)',
      minTempLabel: 'Temp Mínima de Conforto (ºC)',
      messageLanguageLabel: 'Idioma da Mensagem',
      runOptimizationAgent: 'Executar Agente de Otimização',
      runningAgent: 'Executando Agente...',
      agentRecommendationTitle: 'Recomendação do Agente',
      awaitingGemini: 'Aguardando o Agente Gemini...',
      estimatedSavingsFinancial: 'Economia Estimada (Financeira)',
      co2ReductionSustainability: 'Redução de CO2 (Sustentabilidade)',
      prescriptiveJustificationLabel: 'Justificativa Prescritiva',
      actionableScriptLabel: 'Script Acionável (Comando IoT)',
      approveAndAutomate: 'Aprovar e Automatizar (Simulado)',
      initialMessage: 'Insira os parâmetros e execute o agente para ver as recomendações.',
      errorLabel: 'Erro',
      validationInputFormatError: 'Formato de entrada inválido. Use números separados por vírgula (ex: 100.0, 110.0).',
      validationMaxMinError: 'A temperatura máxima deve ser maior que a temperatura mínima de conforto.'
    },
    es: {
      mainTitle: 'Agente de Optimización de Energía Twin Transition',
      headerSubtitle: 'Análisis Prescriptivo y Reducción de Huella de Carbono para PYMEs Europeas.',
      uxTip: 'Utilizando patrones de consumo de energía + pronóstico del tiempo, el agente calcula ajustes óptimos de HVAC para minimizar costos y CO₂ sin comprometer la comodidad.',
      paramsTitle: 'Parámetros de Optimización',
      simulatedConsumptionLabel: 'Consumo Simulado (kWh, últimas 3 horas)',
      simulatedConsumptionHint: 'Simulación simple de datos históricos para el MVP.',
      weatherForecastLabel: 'Pronóstico del Tiempo (ºC, próximas 3 horas)',
      weatherForecastHint: 'Simulación del pronóstico del tiempo para correlación.',
      maxTempLabel: 'Temp Máxima de Confort (ºC)',
      minTempLabel: 'Temp Mínima de Confort (ºC)',
      messageLanguageLabel: 'Idioma del Mensaje',
      runOptimizationAgent: 'Ejecutar Agente de Optimización',
      runningAgent: 'Ejecutando Agente...',
      agentRecommendationTitle: 'Recomendación del Agente',
      awaitingGemini: 'Esperando el Agente Gemini...',
      estimatedSavingsFinancial: 'Ahorro Estimado (Financiero)',
      co2ReductionSustainability: 'Reducción de CO2 (Sustentabilidad)',
      prescriptiveJustificationLabel: 'Justificación Prescriptiva',
      actionableScriptLabel: 'Script Accionable (Comando IoT)',
      approveAndAutomate: 'Aprobar y Automatizar (Simulado)',
      initialMessage: 'Ingrese los parámetros y ejecute el agente para ver las recomendaciones.',
      errorLabel: 'Error',
      validationInputFormatError: 'Formato de entrada inválido. Utilice números separados por comas (ej: 100.0, 110.0).',
      validationMaxMinError: 'La temperatura máxima debe ser mayor que la temperatura mínima de confort.'
    }
  };

  // Função para retornar o texto traduzido (Método simples)
  T(key: string): string { 
    const lang = this.selectedLanguage || 'en';
    const dict = this.translations[lang] || this.translations['en'];
    return dict[key] || this.translations['en'][key] || key;
  }
  
  // Inicia o serviço de loading de UX (simula o tempo de cálculo LLM)
  private startUxSimulation() {
    let index = 0;
    const interval$ = timer(0, 1000).pipe(
      tap(() => {
        index = (index + 1) % this.loadingMessages.length;
        this.loadingMessage.set(this.T(this.loadingMessages[index]));
      })
    );
    return interval$.subscribe();
  }

  formattedSavings = computed(() => {
    const rec = this.agentRecommendation();
    if (rec && rec.estimatedCostSavingsEur) { 
      return rec.estimatedCostSavingsEur.toFixed(2);
    }
    return '0.00';
  });

  private parseInput(input: string): number[] | null {
    const parts = input.split(',').map(s => s.trim());
    const numbers = parts.map(s => parseFloat(s));
    
    if (numbers.some(isNaN)) {
      return null;
    }
    return numbers;
  }

  runAgent(): void {
    // Inicia a simulação de UX
    const uxSubscription = this.startUxSimulation();
    
    this.isLoading.set(true);
    this.errorMessage.set(null);
    this.successMessage.set(null); 
    this.agentRecommendation.set(null);

    const consumption = this.parseInput(this.consumptionInput);
    const weather = this.parseInput(this.weatherInput);

    if (!consumption || !weather) {
      this.errorMessage.set(this.T('validationInputFormatError')); 
      this.isLoading.set(false);
      uxSubscription.unsubscribe();
      return;
    }
    
    if (this.maxTemp <= this.minTemp) {
        this.errorMessage.set(this.T('validationMaxMinError')); 
        this.isLoading.set(false);
        uxSubscription.unsubscribe();
        return;
    }


    const request: OptimizationRequest = {
      historicalConsumptionKwH: consumption,
      weatherForecastDegreesC: weather,
      limits: {
        maxTemp: this.maxTemp,
        minComfortTemp: this.minTemp
      },
      selectedLanguage: this.selectedLanguage 
    };

    // Adiciona um pequeno delay simulado para que o spinner de UX rode por pelo menos 1s
    timer(1000).subscribe(() => {
        this.energyService.generateRecommendation(request, this.selectedLanguage).subscribe({
            next: (response: ApiResponse<OptimizationRecommendation>) => {
                if (response.data) {
                    this.agentRecommendation.set(response.data);
                    this.successMessage.set(response.message); 
                } else {
                    this.errorMessage.set(response.message || 'Optimization failed without specific data returned.');
                }
            },
            error: (err: any) => {
                console.error('API Error:', err);
                let errorMsg = 'An unknown error occurred while connecting to the backend.';
                if (err.status === 400) {
                    errorMsg = 'Validation Error: Check input parameters.';
                } else if (err.status > 0) {
                    errorMsg = `Connection failed (Status ${err.status}). Ensure Python/Spring services are running.`;
                }
                this.errorMessage.set(errorMsg);
            },
            complete: () => {
                this.isLoading.set(false);
                uxSubscription.unsubscribe(); // Para a simulação de UX
            }
        });
    });
  }
}