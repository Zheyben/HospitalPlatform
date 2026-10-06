# Catalogo clinico sintetico para la demo academica

`load_synthetic_clinical_catalog.sql` contiene **datos ficticios**: un codigo de
diagnostico `DEMO-DX-001`, dos medicamentos, tres presentaciones y dos
procedimientos. No representan CIE-10, productos farmaceuticos, concentraciones,
dosis ni prestaciones reales. No sirven para atencion clinica.

La fuente registrada es `HospitalPlatform synthetic demo catalog`, version
`demo-v1`, con referencia `SYNTHETIC-DEMO-ONLY; no clinical validity`. El archivo
en este repositorio es la fuente; obtener su checksum con:

```powershell
Get-FileHash database/demo/load_synthetic_clinical_catalog.sql -Algorithm SHA256
```

## Carga

1. Ejecutar las migraciones Flyway V1-V18 con el propietario del esquema.
2. Crear una cuenta sintetica ADMIN activa mediante el procedimiento de
   provisionamiento del entorno. No usar datos personales ni una UUID inventada.
3. Ejecutar con `psql` como propietario del esquema, indicando el correo de esa
   cuenta. Usar variables de conexion seguras del entorno, sin guardar la clave
   en Git:

```powershell
psql -X -v ON_ERROR_STOP=1 -v "approver_email=admin-demo@example.test" -f database/demo/load_synthetic_clinical_catalog.sql
```

El script se ejecuta en una transaccion, exige una cuenta ADMIN activa y se
puede repetir sin crear duplicados. Si faltan permisos, la cuenta aprobadora o
una fila existente coincide por ID pero difiere del paquete, falla y revierte
la carga. El rol runtime restringido no debe ejecutar este script.

Verificar los recuentos de este paquete y su fuente antes de la demostracion:

```sql
SELECT catalog_type, source_name, source_version, license_reference
FROM medical_catalog_sources
WHERE source_name = 'HospitalPlatform synthetic demo catalog';
SELECT count(*) FROM icd10_codes WHERE code LIKE 'DEMO-DX-%';
SELECT count(*) FROM medications WHERE generic_name LIKE '[DEMO] %';
SELECT count(*) FROM medication_presentations WHERE name LIKE '[DEMO] %';
SELECT count(*) FROM procedures WHERE code LIKE 'DEMO-PROC-%';
```

Los recuentos esperados son tres fuentes, un diagnostico, dos medicamentos,
tres presentaciones y dos procedimientos. Las pruebas de integracion de carga
quedan pendientes de ejecutarse en un equipo con PostgreSQL/Docker disponible.
