# Design System — Notify Later

## Design Philosophy

Notify Later deve transmitir que as notificações não estão sendo bloqueadas — estão **esperando o momento certo**.

> **Minimalista, fofo, calmo e moderno.** Formas arredondadas, bastante espaço vazio e detalhes suaves. O roxo representa tecnologia/confiança, o amarelo representa tempo e atenção.

> Se o Notify Later estiver funcionando, o usuário quase não precisa abrir o app.

## Paleta do Logo

| Uso | Nome | HEX |
|-----|------|-----|
| 🟣 Primary | Roxo vibrante — identidade principal | `#6E51F7` |
| 🟣 Dark | Profundidade/contraste | `#4634C9` |
| 🟣 Medium | Secundário | `#6347ED` |
| 🤍 Background | Branco/lilás suave | `#F2F2FC` |
| 🟡 Accent | Amarelo relógio | `#FEC92F` |
| 🟠 Badge | Dourado | `#FEBA2E` |
| 🔵 Light | Lilás claro detalhes | `#D0CDFC` |

**Uso recomendado:** `Primary #6E51F7` como cor principal; `YellowClock` apenas para destaques de tempo (badges, relógio, contadores). Não usar todas simultaneamente.

Implementação em Compose: `ui/theme/Color.kt` (`PurplePrimary`, `PurpleDark`, `YellowClock`, etc.) e `Theme.kt` com `lightColorScheme`/`darkColorScheme` e `Shapes` arredondados (14–28dp). `dynamicColor = false` por padrão para manter identidade.

## Princípios

1. **Quiet by default** — whitespace generoso, sem gradientes agressivos.
2. **One decision at a time** — uma pergunta por tela.
3. **Native first** — Material 3 + shapes 20–24dp.
4. **No dark patterns** — consentimento explícito.
5. **Privacy is visible** — dados locais visíveis.
6. **The app should disappear** — set-and-forget.

## Elementos Visuais

Usar: cantos arredondados (20dp cards, 28dp hero), cards suaves com `elevation 1dp`, ícones **Rounded** (`notifications`, `schedule`, `snooze`, `history`, `apps`, `settings`), formas geométricas, badges em pílula, ilustrações minimalistas, animações suaves 150–200ms, sombras discretas, contraste suave.

Evitar: vermelho dominante, sombras fortes, gradientes excessivos, ícones muito detalhados, estética infantil, alarmes/emergência.

## O detalhe “fofo”

O **relógio amarelo integrado ao sino** vira elemento recorrente:

- 🔔 notificação normal
- 🔔 + 🟡 relógio = agendada
- 🔔 + animação circular = esperando
- `BellWithClockBadge` em `ui/components/NotifyLaterComponents.kt`

Regra visual: **🔔 + 🕐 = esta notificação está esperando.**

## Tokens

| Token | Light | Dark |
|-------|-------|------|
| `primary` | `#6E51F7` | `#B9A6FF` |
| `primaryContainer` | `#E9E7FD` | `#2E227A` |
| `secondary` | `#6347ED` | `#D0CDFC` |
| `tertiary` | `#FEC92F` | `#FEC92F` |
| `background` | `#F2F2FC` | `#14131F` |
| `surface` | `#FFFFFF` | `#1E1D2A` |
| `surfaceVariant` | `#EDE9FF` | `#2D2B45` |
| `surfaceContainer` | `#F4F2FD` | `#1E1D2A` |
| `surfaceContainerHigh` | `#EEECFA` | `#292738` |
| `outlineVariant` | `#E8E3FF` | `#3A3770` |

Os tokens de superfície seguem os níveis tonais do Material 3 (`surfaceContainerLowest` → `surfaceContainerHighest`), para que a elevação funcione em ambos os temas sem sombras arbitrárias.

## Navegação

As quatro áreas principais — **Início, Fila, Registos e Definições** — vivem numa `NavigationBar` Material 3 (3–5 destinos). Onboarding, Aplicações e Detalhes são ecrãs secundários e escondem a barra. O sistema Back e o gesto preditivo continuam a funcionar; nenhum ecrã prende o utilizador.

## Componentes

- **AppBar** — `TopAppBar` com `containerColor = background`, sem branding extra. Nos ecrãs principais não há seta de voltar (a barra inferior resolve a navegação).
- **SoftCard** — `RoundedCornerShape(22dp)`, `containerColor = surface`, `elevation 1dp`. É o único contentor de cartão do sistema.
- **QuietHero** — cartão roxo `primary` com o logo 56dp, um arco de relógio desenhado em `Canvas` e pílulas de estado (`Watching quietly` / `Notification access needed`). O arco anima uma única vez ao entrar na Home (1100 ms, `FastOutSlowInEasing`) e respeita a escala de animação do sistema.
- **ActionTile** — tile com altura mínima de 142dp para a grelha 2×2 da Home; cresce com a escala de fonte graças a `IntrinsicSize.Min` na linha. `accent = true` usa o amarelo do relógio para a Fila, o resto fica em `primaryContainer`.
- **QuietEmptyState** — halo lilás de 124dp, círculo de superfície de 92dp, sino e badge amarelo de relógio. Usado em Fila, Registos, Aplicações e Detalhes.
- **InitialAvatar** — inicial da aplicação num dos quatro tons da marca (nunca cores arbitrárias por hash).
- **TimeBadge** — pílula amarela com ícone `Schedule`, para horas e contadores de tempo.
- **NotifyScaffold** — estrutura comum de ecrã: `TopAppBar`, insets e fundo. Evita divergência entre telas.
- **Buttons** — filled primary 14–18dp, text secondary. Um primário por ecrã.

## Estados

- **Vazio** — sempre uma ação ou uma explicação, nunca apenas “nada aqui”.
- **Permissão** — o cartão de acesso a notificações mostra o estado real (`Granted` / `Not granted`) e um botão que abre as definições do sistema.
- **Acessibilidade** — alvos de toque ≥ 48dp, `contentDescription` em ícones de navegação, texto com contraste ≥ 4.5:1, tipografia em `sp`.

Ver telas: `feature/home/HomeScreen.kt`, `feature/onboarding/OnboardingScreen.kt`, `feature/logs/LogsScreen.kt`, `feature/settings/SettingsScreen.kt`, `navigation/AppShell.kt`.
