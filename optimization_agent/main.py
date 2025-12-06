from fastapi import FastAPI, HTTPException
from models import OptimizationRequest, OptimizationRecommendation 
from optimization_agent import run_optimization_agent 


app = FastAPI(
    title="Twin Transition Optimization Agent",
    description="Core Microservice for ML forecasting and Gemini-powered prescriptive reasoning."
)

# Endpoint: POST /api/v1/optimize
@app.post("/api/v1/optimize", response_model=OptimizationRecommendation)
def optimize_energy(request: OptimizationRequest): 
    """
    Recebe dados de otimização do Gateway Java e retorna a recomendação inteligente.
    """
    # Acessar o campo usando o nome snake_case do modelo Pydantic
    print(f"FASTAPI RECEBIDO: {request.historical_consumption_kwh[:3]}...") 
    
    try:
        recommendation_data = run_optimization_agent(
            historical_data_path='simulated_energy_data.csv',
            temp_forecast=request.weather_forecast_degrees_c, 
            limits=request.limits.dict(),
            selected_language=request.selected_language # <--- CAMPO AGORA EXISTE NO REQUEST
        )
        
        return OptimizationRecommendation(**recommendation_data)
        
    except FileNotFoundError:
        raise HTTPException(status_code=500, detail="Historical data file not found on the Python server.")
    except Exception as e:
        print(f"Agent processing error: {e}")
        raise HTTPException(status_code=500, detail=f"Internal Agent Error: {e}")

if __name__ == '__main__':
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)