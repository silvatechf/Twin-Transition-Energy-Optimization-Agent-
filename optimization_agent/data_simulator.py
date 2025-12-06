# optimization_agent/data_simulator.py
import pandas as pd
import numpy as np
from datetime import datetime, timedelta

def generate_simulated_data(days=30):
    """
    Generates simulated hourly data for energy consumption and weather, 
    mimicking a commercial building's pattern over 30 days.
    """
    print(f"Generating {days} days of simulated energy data...")
    
    start_date = datetime.now() - timedelta(days=days)
    # Generate hourly index
    # (Mantivemos 'H' por enquanto; a FutureWarning será resolvida em futuras atualizações)
    hours = pd.date_range(start=start_date, periods=days * 24, freq='H') 
    
    # --- 1. Simulate Energy Consumption (kWh) ---
    base_consumption = 10 
    
    # Daily cycle: peak during work hours (9h-18h)
    daily_cycle = np.sin((hours.hour - 6) / 24 * 2 * np.pi) * 8 + 5
    
    # CORREÇÃO CRÍTICA: Usar np.where para aplicar lógica condicional de forma segura.
    # Se a hora for < 6, define o valor como 2 (Low consumption at night), caso contrário, usa o valor de daily_cycle
    daily_cycle = np.where(hours.hour < 6, 2, daily_cycle)
    # Se a hora for > 18, define o valor como 4 (Low consumption in the evening), caso contrário, usa o valor atual de daily_cycle
    daily_cycle = np.where(hours.hour > 18, 4, daily_cycle) 
    
    # Weekly variation: lower consumption on weekends
    # Se for sábado (5) ou domingo (6), o fator é 0.5 (metade do consumo), caso contrário é 1.0.
    weekend_factor = np.where((hours.dayofweek >= 5), 0.5, 1.0) 
    
    # Trend and Noise
    noise = np.random.normal(0, 1.5, len(hours))
    
    consumption = (base_consumption + daily_cycle) * weekend_factor + noise
    consumption[consumption < 0] = 0 # Ensure consumption is non-negative
    
    # --- 2. Simulate Temperature (C) ---
    base_temp = 15
    # Diurnal temperature cycle
    temp_cycle = np.sin((hours.hour - 8) / 24 * 2 * np.pi) * 8 + 5
    temperature = base_temp + temp_cycle + np.random.normal(0, 1, len(hours))

    df = pd.DataFrame({
        'timestamp': hours,
        'consumption_kwh': consumption,
        'temperature_c': temperature
    }).set_index('timestamp')
    
    print(f"Data generation complete. Total records: {len(df)}")
    return df

if __name__ == '__main__':
    # Run this script to generate the required CSV file
    df = generate_simulated_data()
    output_path = 'simulated_energy_data.csv'
    df.to_csv(output_path)
    print(f"Saved simulated data to {output_path}")