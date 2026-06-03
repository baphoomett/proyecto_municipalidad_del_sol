# Publishes a test event to RabbitMQ via the Management HTTP API
# Requires RabbitMQ management enabled on localhost:15672 and guest/guest credentials

$apiUrl = 'http://localhost:15672/api/exchanges/%2f/integracion.exchange/publish'
$payload = '{"tipo":"NUEVO_FOCO","zona":"Valle del Sol","mensaje":"Incendio detectado"}'

$body = @{
    properties = @{}
    routing_key = 'integracion.routing'
    payload = $payload
    payload_encoding = 'string'
} | ConvertTo-Json -Compress

Invoke-RestMethod -Uri $apiUrl -Method Post -Credential (New-Object System.Management.Automation.PSCredential('guest',(ConvertTo-SecureString 'guest' -AsPlainText -Force))) -Body $body -ContentType 'application/json'
Write-Host "Evento publicado."
