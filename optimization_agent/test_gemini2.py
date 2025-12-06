from google import genai
import os

print("SDK carregado com sucesso.")

api_key = os.getenv("GEMINI_API_KEY")
print("Chave encontrada?", api_key is not None, " | Valor:", api_key)

client = genai.Client(api_key=api_key)

print("\nListando modelos disponíveis...")
models = client.models.list()

for m in models.models:
    print(" -", m.name)

print("\nTestando geração:")
response = client.models.generate_content(
    model=models.models[0].name,
    contents="Olá, funcionando?"
)

print("\nRESPOSTA DO MODELO:")
print(response.text)
