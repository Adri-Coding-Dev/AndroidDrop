# AndroidDrop — Software Design Document (SDD)

> **Version:** 1.0  
> **Estado:** Pendiente de aprobacion  
> **Audiencia:** Arquitectos, desarrolladores Android, revisores de seguridad

---

## Indice

1. [Resumen Ejecutivo](SDD-01-Resumen-Ejecutivo.md)
2. [Principios de Diseno](SDD-02-Principios.md)
3. [Arquitectura General](SDD-03-Arquitectura.md)
4. [Estructura de Modulos](SDD-04-Modulos.md)
5. [Decisiones Tecnicas](SDD-05-Decisiones-Tecnicas.md)
6. [Flujo de Datos y Protocolo](SDD-06-Flujo-Datos.md)
7. [Sistema de Diseno](SDD-07-Sistema-Diseno.md)
8. [Motor de Animaciones](SDD-08-Motor-Animation.md)
9. [Seguridad](SDD-09-Seguridad.md)
10. [Rendimiento y Accesibilidad](SDD-10-Rendimiento.md)
11. [Diagramas](SDD-11-Diagramas.md)
12. [Escalabilidad Futura](SDD-12-Escalabilidad.md)

---

## 1. Resumen Ejecutivo

### 1.1 Proposito

AndroidDrop es una aplicacion de transferencia de archivos entre dispositivos Android basada en proximidad fisica. A diferencia de soluciones existentes (ShareIt, Xender, Nearby Share), AndroidDrop prioriza la **experiencia sensorial** por encima de la mera transferencia de datos. La metafora central es "pasar el archivo como si fuera un objeto fisico".

### 1.2 Objetivos Estrategicos

| Objetivo | Metrica | Target |
|---|---|---|
| UX fluida | Latencia percepcion visual | < 16ms por frame |
| Transferencia rapida | Velocidad de transferencia | > 50 MB/s en Wi-Fi Direct |
| Sin dependencia externa | Funcionamiento offline | 100% local |
| Seguridad | Cifrado E2E por sesion | AES-256-GCM |
| Escalabilidad | Anadir nueva plataforma | < 2 semanas ingenieria |

### 1.3 Diferenciacion

| Aspecto | Competencia | AndroidDrop |
|---|---|---|
| Experiencia | Funcional, generica | Inmersiva, metaforica |
| Animaciones | Basicas o inexistentes | Fisicas, sincronizadas entre dispositivos |
| Feedback | Notificaciones estandar | Haptico + visual + sonoro |
| Sincronizacion | Independiente por dispositivo | Esfera unica compartida |

### 1.4 Stakeholders y Roles

| Rol | Responsabilidad |
|---|---|
| Arquitecto de Software | Definir estructura, patrones, decisiones tecnicas |
| Ingeniero Android | Implementar modulos, pruebas, integracion |
| Disenador UI/UX | Sistema de diseno, animaciones, experiencia |
| Ingeniero de Redes | Protocolo de comunicacion, optimizacion |
| Ingeniero de Seguridad | Cifrado, autenticacion, proteccion MITM |
| Ingeniero de Rendimiento | Optimizacion de memoria, CPU, bateria, GPU |
| Especialista en Accesibilidad | WCAG, TalkBack, contraste, motion |
