# Opti System FF

**FF Performance Engine** — plataforma Android modular de diagnóstico, preparação e otimização responsável para Free Fire e Free Fire MAX.

O objetivo é construir motores especializados e sistemas proprietários, não um limpador genérico de memória. Cada capacidade precisa ter uma fonte real de dados, política de compatibilidade, testes e evidência antes de ser apresentada como otimização eficaz.

## Estado atual — Foundation 0.1.0
- Projeto Android nativo em Kotlin + Jetpack Compose.
- Módulos separados para modelos, motor de decisão, diagnósticos e perfis de jogo.
- Painel com RAM disponível no sistema, armazenamento livre e estado térmico reportado pelo Android.
- Detecção e abertura dos pacotes de Free Fire e Free Fire MAX.
- Recomendações determinísticas e testes unitários.
- GitHub Actions para testes, montagem do APK debug e publicação do artefato.
- Registro de recursos implementados, planejados e indisponíveis.

**Importante:** esta primeira versão é somente leitura. Ainda não aplica tweaks, não encerra processos, não altera configurações do Android e não modifica arquivos dos jogos. Também não mede FPS real, frametime, uso de GPU/CPU do processo do jogo ou latência de toque.

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
| core:model | Contratos e modelos sem dependência Android |
| core:engine | Motor puro de análise e recomendações |
| core:diagnostics | Coleta de dados públicos do Android |
| core:games | Detecção e abertura dos jogos |

A arquitetura prevê evolução para Performance Director, Adaptive Resource Manager, Frame Stability Intelligence, Thermal Prediction, Background Interference Controller, Network Stability Intelligence, Input Consistency Engine, Benchmark Lab, Conflict Detector e Recovery System. Esses subsistemas só serão ativados quando suas fontes, permissões e resultados forem verificáveis.

## Plataforma de referência
- Samsung Galaxy A06 SM-A065M
- Android 16 / API 36 no dispositivo
- arm64-v8a
- Sem root como requisito

O suporte a outros aparelhos será adicionado por perfis e validação por API/OEM. O nome do aparelho, sozinho, não garante que um recurso de hardware esteja acessível.

## Stack
- Kotlin 2.4.21
- Android Gradle Plugin 9.1.1
- Gradle 9.3.1
- Jetpack Compose BOM 2026.06.00 (linha Compose 1.11)
- AndroidX Activity Compose 1.12.4
- AndroidX Core KTX 1.17.0
- JDK 17
- Android compileSdk 36 / targetSdk 36

A base está fixada em versões estáveis de AndroidX que compilam com API 36. O alvo de instalação continua em minSdk 26; o app não exige Android 37.

O workflow do GitHub Actions instala Gradle e SDK Android necessários.

## Build e testes
Com JDK 17, Android SDK (platform 36) e Gradle 9.3.1 instalados:

    gradle --no-daemon testDebugUnitTest
    gradle --no-daemon assembleDebug

O APK debug será gerado em app/build/outputs/apk/debug/app-debug.apk.

No GitHub, o workflow Android CI executa testes e montagem do APK a cada push/PR nas branches configuradas. O artefato fica disponível por 14 dias.

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
- Session checkpoint: docs/SESSION_CHECKPOINT.md
- Repository rules: AGENTS.md
