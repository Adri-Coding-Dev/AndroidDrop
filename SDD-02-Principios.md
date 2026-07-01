## 2. Principios de Diseno

### 2.1 UX First
Cada decision tecnica se evalua primero por su impacto en la experiencia de usuario. Una funcion optima tecnicamente pero que degrade la experiencia sera descartada.

### 2.2 Performance First
Todas las animaciones y operaciones deben ejecutarse a 60 FPS (idealmente 120 FPS en dispositivos compatibles). Sin jank, sin micro-pausas.

### 2.3 Clean Architecture + SOLID
Separacion estricta en capas: Presentation -> Domain -> Data. Cada modulo expone interfaces; las implementaciones son inyectadas.

### 2.4 Modularidad Absoluta
Cada funcionalidad es un modulo Gradle independiente. Comunicacion exclusivamente mediante interfaces e inyeccion de dependencias.

### 2.5 Seguridad por Defecto
Cifrado obligatorio en toda comunicacion. Sin datos en claro en ningun punto del pipeline. Sin telemetria ni servidores externos.

### 2.6 Offline-First
La aplicacion funciona completamente sin Internet. No hay dependencia de servicios en la nube.

### 2.7 Single Source of Truth
Cada pieza de datos tiene una unica fuente de verdad. Los ViewModels exponen StateFlow. Los repositorios centralizan el acceso.

### 2.8 Documentation-Driven Development
Todo codigo incluye comentarios utiles que expliquen el "por que", no el "que". KDoc en todas las interfaces y metodos publicos.
