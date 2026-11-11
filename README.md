# Opti System FF

**FF Performance Engine** — plataforma Android modular de diagnóstico, preparação e otimização responsável para Free Fire e Free Fire MAX.

O objetivo é construir motores especializados e sistemas proprietários, não um limpador genérico de memória. Cada capacidade precisa de uma fonte real de dados, política de compatibilidade, testes e evidência antes de ser apresentada como otimização eficaz.

## Estado atual — 0.2.0

A versão 0.2.0 acrescenta:
- Device Intelligence para memória do sistema, perfil low-RAM, temperatura, modo de economia, bateria e tela.
- Leitura de fabricante/modelo do SoC e ABIs quando o Android expõe esses campos.
- Coleta sob demanda, sem polling em background ou serviço permanente.
- Game Session Lab para iniciar e finalizar sessões de Free Fire e Free Fire MAX.
- Snapshots do estado do sistema antes/depois e comparador Kotlin puro.
- Histórico privado limitado a 20 sessões.
- Diagnósticos pré-partida ampliados e testes unitários.

**Importante:** a versão 0.2.0 ainda não aplica tweaks nem altera configurações do Android. Ela não mede FPS real, frametime, uso de GPU/CPU/RAM do processo do jogo, latência de toque ou ping até os servidores da Garena. A comparação da sessão descreve o sistema inteiro e não atribui uma mudança exclusivamente ao jogo.

## Jogos suportados

| Perfil | Pacote |
|---|---|
| Free Fire | com.dts.freefireth |
| Free Fire MAX | com.dts.freefiremax |

A detecção é limitada a esses identificadores no manifesto; a permissão ampla QUERY_ALL_PACKAGES não é solicitada.

## Arquitetura

| Módulo | Função |
|---|---|
| app | Interface, fluxo de usuário e composição |
| core:model | Contratos, snapshots e sessões sem dependência Android |
| core:engine | Diagnósticos e comparação de sessões em Kotlin puro |
| core:diagnostics | Coleta sob demanda por APIs públicas do Android |
| core:games | Detecção e abertura dos jogos |
| core:session | Histórico de sessões privado no aparelho |

Sistemas futuros previstos: Performance Director, Adaptive Resource Manager, Frame Stability Intelligence, Thermal Prediction, Background Interference Controller, Network Stability Intelligence, Input Consistency Engine, Benchmark Lab, Conflict Detector e Recovery System. Cada sistema só será ativado quando suas fontes, permissões e resultados forem verificáveis.

## Plataforma de referência
- Samsung Galaxy A06 SM-A065M
- Android 16 / API 36 no dispositivo
- arm64-v8a
- Sem root como requisito

O usuário confirmou que a base 0.1.0 instalou e abriu no Galaxy A06. Os valores reais do SoC e das taxas de atualização reportados pela API ainda devem ser conferidos em uma instalação 0.2.0.

## Stack
- Kotlin 2.4.21
- Android Gradle Plugin 9.1.1
- Gradle 9.3.1
- Jetpack Compose BOM 2026.06.00 (linha Compose 1.11)
- AndroidX Activity Compose 1.12.4
- AndroidX Core KTX 1.17.0
- JDK 17
- Android compileSdk 36 / targetSdk 36 / minSdk 26

## Build e testes
Com JDK 17, Android SDK (platform 36) e Gradle 9.3.1 instalados:

    gradle --no-daemon testDebugUnitTest
    gradle --no-daemon assembleDebug

O APK debug será gerado em app/build/outputs/apk/debug/app-debug.apk.

O GitHub Actions executa testes e montagem do APK a cada push/PR nas branches configuradas. O artefato fica disponível por 14 dias.

## Segurança e integridade
- Sem root obrigatório.
- Sem injeção, alteração de memória, edição de arquivos dos jogos ou interferência no anti-cheat.
- Sem encerramento indiscriminado de processos.
- Sem tweaks ocultos ou alterações automáticas irreversíveis.
- Shizuku/ADB será opcional e condicionado a permissões e capacidades reais.
- Nenhum ganho de FPS, ping, temperatura ou resposta de toque será prometido sem medição adequada.

## Documentação
- Architecture: docs/ARCHITECTURE.md
- Safety model: docs/SAFETY_MODEL.md
- Roadmap: docs/ROADMAP.md
- Device support: docs/DEVICE_SUPPORT.md
- Optimization registry: docs/OPTIMIZATION_REGISTRY.md
- Session protocol: docs/SESSION_PROTOCOL.md
- Session checkpoint: docs/SESSION_CHECKPOINT.md
- Repository rules: AGENTS.md
