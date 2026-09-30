# 10 - TEST PLAN v1.3.1 FINAL

**Control de vigencia E.3.2 (30/09/2026):** los casos FT de este plan son **diseño de pruebas**, no evidencia de ejecución. El alcance actual excluye evaluación clínica de triaje y registro de diagnóstico (baseline/ADR-007); FT-011–FT-013 se conservan como antecedentes fuera del MVP actual. Dashboard carece de módulo funcional; FT-015/FT-016 son propuestas. RF-014 canónico conserva la invariante de exclusión mutua probada con **dos** solicitudes en `AppointmentModuleIT`; el ensayo SRS de **20** sigue pendiente (DEC-023). No se deduce cobertura por la presencia de una fila FT.

## Mejoras aplicadas en esta versión

-   Incorporación de prioridad por caso de prueba.
-   Incorporación de evidencia esperada por caso.
-   Mejora de trazabilidad QA.

------------------------------------------------------------------------

# 1. Prioridad de casos de prueba

Cada caso de prueba debe clasificarse:

  -----------------------------------------------------------------------
  Prioridad                           Descripción
  ----------------------------------- -----------------------------------
  CRÍTICO                             Bloquea funcionalidades esenciales,
                                      seguridad o flujo asistencial.

  ALTO                                Afecta funcionalidades principales
                                      del MVP.

  MEDIO                               Afecta funcionalidades importantes
                                      pero no bloquea operación.

  BAJO                                Problemas menores visuales o
                                      mejoras secundarias.
  -----------------------------------------------------------------------

------------------------------------------------------------------------

# 2. Matriz de casos funcionales con prioridad y evidencia

## Autenticación

  ---------------------------------------------------------------------------
  ID             Caso           Prioridad      Resultado       Evidencia
                                               esperado        
  -------------- -------------- -------------- --------------- --------------
  FT-001         Login válido   CRÍTICO        Usuario accede  Captura
                                               correctamente   pantalla +
                                                               registro
                                                               sesión

  FT-002         Credenciales   CRÍTICO        Acceso          Mensaje
                 inválidas                     rechazado       error + log
                                                               seguridad

  FT-003         Usuario sin    ALTO           Acceso          Respuesta
                 permisos                      restringido     autorización
  ---------------------------------------------------------------------------

------------------------------------------------------------------------

## Gestión de citas

  -------------------------------------------------------------------------------
  ID             Caso             Prioridad      Resultado       Evidencia
                                                 esperado        
  -------------- ---------------- -------------- --------------- ----------------
  FT-004         Consultar        ALTO           Horarios        Captura
                 disponibilidad                  visibles        disponibilidad

  FT-005         Reservar cita    CRÍTICO        Cita creada     Respuesta API +
                                                 correctamente   registro BD

  FT-006         Cancelar cita    ALTO           Estado          Cambio estado +
                                                 actualizado     auditoría

  FT-007         Reprogramar cita ALTO           Nueva fecha     Registro
                                                 registrada      modificación

  FT-008         Reserva          CRÍTICO        Solo una        Resultado
                 concurrente                     reserva         transacción
                                                 aceptada        
  -------------------------------------------------------------------------------

------------------------------------------------------------------------

## Recepción

  --------------------------------------------------------------------------
  ID             Caso           Prioridad      Resultado      Evidencia
                                               esperado       
  -------------- -------------- -------------- -------------- --------------
  FT-009         Realizar       ALTO           Paciente       Cambio
                 check-in                      admitido       estado +
                                                              auditoría

  FT-010         Consultar      MEDIO          Lista          Captura
                 citas del día                 disponible     pantalla
  --------------------------------------------------------------------------

------------------------------------------------------------------------

## Triaje — casos históricos fuera del MVP actual

  --------------------------------------------------------------------------
  ID             Caso           Prioridad      Resultado      Evidencia
                                               esperado       
  -------------- -------------- -------------- -------------- --------------
  FT-011         Registrar      CRÍTICO        Datos          Registro
                 evaluación                    guardados      evaluación

  FT-012         Actualizar     CRÍTICO        Cambio         Historial
                 estado                        permitido      auditoría
                 paciente                                     
  --------------------------------------------------------------------------

------------------------------------------------------------------------

## Atención profesional — distinguir flujo actual de registro clínico histórico

  --------------------------------------------------------------------------
  ID             Caso           Prioridad      Resultado      Evidencia
                                               esperado       
  -------------- -------------- -------------- -------------- --------------
  FT-013         Registrar      CRÍTICO        Atención       Registro
                 atención                      creada         atención

  FT-014         Finalizar      ALTO           Estado         Cambio
                 atención                      actualizado    workflow
  --------------------------------------------------------------------------

------------------------------------------------------------------------

## Dashboard — pruebas propuestas sin módulo funcional

  --------------------------------------------------------------------------
  ID             Caso           Prioridad      Resultado      Evidencia
                                               esperado       
  -------------- -------------- -------------- -------------- --------------
  FT-015         Consultar      MEDIO          Datos visibles Captura
                 métricas                                     dashboard

  FT-016         Aplicar        MEDIO          Información    Resultado
                 filtros fecha                 actualizada    consulta
  --------------------------------------------------------------------------

------------------------------------------------------------------------

# 3. Matriz requisito - prueba

Cada requisito debe relacionarse:

``` text
Requisito

↓

Caso prueba

↓

Prioridad

↓

Resultado esperado

↓

Evidencia

↓

Estado ejecución
```

------------------------------------------------------------------------

# 4. Ejemplo trazabilidad QA

  Requisito                       Caso     Prioridad   Estado
  ------------------------------- -------- ----------- -----------
  Usuario puede reservar cita     FT-005   CRÍTICO     Pendiente
  Recepción admite paciente       FT-009   ALTO        Pendiente
  Profesional registra atención   FT-013   CRÍTICO     Pendiente

------------------------------------------------------------------------

# 5. Evidencia de ejecución

Cada prueba debe registrar:

-   caso ejecutado;
-   usuario utilizado;
-   fecha;
-   resultado esperado;
-   resultado obtenido;
-   evidencia;
-   responsable.

Formato:

``` text
Caso prueba

↓

Resultado esperado

↓

Resultado obtenido

↓

Evidencia

↓

Estado final
```

------------------------------------------------------------------------

# 6. Estado documental

Documento actualizado como:

**10-TEST-PLAN v1.3.1 FINAL**

Incluye:

-   pruebas funcionales;
-   pruebas RBAC;
-   workflow asistencial;
-   seguridad;
-   prioridades QA;
-   evidencia esperada;
-   trazabilidad de pruebas.
