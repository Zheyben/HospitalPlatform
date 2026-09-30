# Project Charter — Hospital de Huaycán

## Información general
Proyecto: Implementación de una plataforma web y móvil para optimizar la gestión de citas médicas y la trazabilidad del flujo de consulta externa en el Hospital de Huaycán - MINSA.

Curso: Curso Integrador I: Sistemas Software.

Tipo: Proyecto académico basado en caso de estudio.

## Contexto
El proyecto se desarrolla tomando como referencia el Hospital de Huaycán — MINSA / DIRIS Lima Este.

La solución propuesta no representa un software oficial del hospital ni de ninguna entidad del MINSA.

## Problema
La gestión de citas médicas y el seguimiento del flujo de consulta externa requieren coordinación entre pacientes, personal administrativo y profesionales de salud. El proyecto busca proponer una plataforma que permita mejorar la organización, trazabilidad y disponibilidad de información operativa.

Los procesos reales serán validados mediante investigación y levantamiento autorizado.

## Objetivo general
Diseñar y desarrollar una plataforma web y móvil orientada a apoyar la gestión de citas médicas y la trazabilidad del flujo de consulta externa del Hospital de Huaycán, incorporando reserva, confirmación, cancelación, reprogramación, recuperación de cupos, lista de espera, auditoría y seguridad.

## Variables
Variable independiente:
Plataforma web y móvil.

Variable dependiente:
Gestión de citas médicas y trazabilidad del flujo de consulta externa.

Palabra articuladora:
Optimizar.

## Alcance MVP
Incluye:
- Backend Spring Boot.
- Plataforma web.
- Aplicación móvil del paciente.
- Usuarios y roles.
- Especialidades, profesionales y agendas.
- Reserva, confirmación, cancelación y reprogramación.
- Lista de espera y recuperación de cupos.
- Flujo operativo de consulta externa.
- Auditoría e indicadores.

## Fuera del alcance inicial
- Historia clínica electrónica completa.
- Diagnóstico médico.
- Prescripción electrónica.
- Farmacia.
- Laboratorio.
- Hospitalización.
- Emergencia.
- Integraciones oficiales MINSA/SIS/SIHCE sin autorización.

## Arquitectura propuesta
- Monolito modular.
- Java 21 + Spring Boot.
- PostgreSQL.
- Next.js/React.
- React Native/Expo.

Paquete raíz:
com.integrador.salud.api

## Restricciones
- Uso académico.
- Datos sintéticos.
- No acceso asumido a sistemas internos.
- No uso de historias clínicas reales.
- Validación progresiva del alcance.

## Criterios de éxito
- Documentación completa.
- Arquitectura definida.
- Backend funcional.
- Web funcional.
- Mobile funcional según alcance.
- Pruebas documentadas.
- Seguridad aplicada.

## Principio de trabajo
INVESTIGAR → VALIDAR → DISEÑAR → IMPLEMENTAR → PROBAR → MEDIR → ESCALAR
