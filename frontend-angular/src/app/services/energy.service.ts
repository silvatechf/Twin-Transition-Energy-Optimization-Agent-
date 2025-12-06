import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse, OptimizationRecommendation, OptimizationRequest } from '../models/optimization.model';

@Injectable({
  providedIn: 'root',
})
export class EnergyService {
  
  // Endpoint do Spring Boot Gateway
  private apiUrl = 'http://localhost:8080/api/v1/optimization/recommend';

  private http = inject(HttpClient);

  generateRecommendation(request: OptimizationRequest, language: string): Observable<ApiResponse<OptimizationRecommendation>> {
    const headers = { 'Accept-Language': language };
    return this.http.post<ApiResponse<OptimizationRecommendation>>(this.apiUrl, request, { headers });
  }
}