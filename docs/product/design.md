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

## Componentes

- **AppBar** — `TopAppBar` com `containerColor = background`, sem branding extra.
- **SoftCard** — `RoundedCornerShape(20dp)`, `containerColor = surface`, `elevation 1dp`.
- **IconBadge / BellWithClockBadge / PillBadge** — em `ui/components/NotifyLaterComponents.kt`.
- **Hero** — card roxo `primary` com logo 64dp + badge amarelo com contador.
- **EmptyState** — círculo `primaryContainer` + sino, título + descrição curta.
- **Buttons** — filled primary 16dp, text secondary. Um primário por tela.

Ver telas: `feature/home/HomeScreen.kt`, `feature/onboarding/OnboardingScreen.kt`, `feature/logs/LogsScreen.kt`, `feature/settings/SettingsScreen.kt`.
