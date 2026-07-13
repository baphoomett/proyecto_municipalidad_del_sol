# Seed script: crea el usuario administrador
# Ejecutar desde la raiz del proyecto: .\scripts\seed_admin.ps1

$email    = "admin@municipalidad.cl"
$password = "Admin1234!"
$fullName = "Administrador"

Write-Host ">>> Registrando usuario admin..."

$body = @{
    email    = $email
    password = $password
    fullName = $fullName
} | ConvertTo-Json

try {
    Invoke-RestMethod -Method Post `
        -Uri "http://localhost:8084/api/auth/register" `
        -Body $body `
        -ContentType "application/json" `
        -ErrorAction Stop
    Write-Host ">>> Usuario registrado."
} catch {
    $status = $_.Exception.Response.StatusCode.value__
    if ($status -eq 409 -or $status -eq 400) {
        Write-Host ">>> El usuario ya existe, continuando con asignacion de rol..."
    } else {
        Write-Host "ERROR al registrar: $_"
        exit 1
    }
}

Write-Host ">>> Asignando ROLE_ADMIN en la base de datos..."

$sql = @"
DO `$`$
DECLARE
    v_user_id BIGINT;
    v_role_id BIGINT;
BEGIN
    SELECT id INTO v_user_id FROM users WHERE email = '$email';
    SELECT id INTO v_role_id FROM roles  WHERE name  = 'ROLE_ADMIN';
    IF v_user_id IS NOT NULL AND v_role_id IS NOT NULL THEN
        DELETE FROM user_roles WHERE user_id = v_user_id;
        INSERT INTO user_roles (user_id, role_id)
        VALUES (v_user_id, v_role_id)
        ON CONFLICT DO NOTHING;
        RAISE NOTICE 'Rol ROLE_ADMIN asignado correctamente.';
    ELSE
        RAISE EXCEPTION 'No se encontro el usuario o el rol ROLE_ADMIN.';
    END IF;
END
`$`$;
"@

docker compose exec -T postgres_ms_usuarios `
    psql -U postgres -d ms_usuarios_db -c $sql

if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: fallo la asignacion del rol en postgres."
    exit 1
}

Write-Host ""
Write-Host "=============================================="
Write-Host "  ADMIN CREADO EXITOSAMENTE"
Write-Host "  Nombre  : $fullName"
Write-Host "  Correo  : $email"
Write-Host "  Password: $password"
Write-Host "=============================================="
