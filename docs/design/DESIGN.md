# DESIGN.md - BooqIn

Layered on top of global-rules.md; where they conflict, this file wins.
Status: DRAFT (owner decisions in section 12 are locked; items marked (call) are proposals).

## 1. Intent
BooqIn is a platform where people lend books to each other: browse a shared catalogue, build collections, borrow from a neighbour, pay the delivery fee.
Promise: find a book, see at once who has it and whether it is free, borrow it in two clicks.
Feeling: **warm, calm, cover-first.**

References and what we take from each (measured):
- **Airbnb (home page):** one family at 14px body / 28px h1 / 700 weight; text `#222` and `#6C6C6C`; accent used on only 7 elements; 20px radius on images; separation by soft layered shadow, not borders; transitions 0.25s with `cubic-bezier(0.2, 0, 0, 1)`; one big pill search as the main control.
- **Open Library (domain):** warm paper neutrals (surface `#FAF9F4`, ink `#3D382E`), grotesk for UI plus a serif for book content, 4px / 8px radii.
- Letterboxd, Vinted, Bookshop.org, Literal could not be measured (bot check); not used.

Must not be: a dark navy admin template, a blue-button SaaS dashboard, a wall of identical bordered cards, a flat coloured square standing in for a missing cover.

## 2. Layout
Desktop first, design width 1280px, content max width 1200px, page gutters 32px (16px on phone).

```
+--------------------------------------------------------------------------+
| BooqIn   Books  Collections  Following     [ search the catalogue ]  cart bell (avatar) |  72px, sticky
+--------------------------------------------------------------------------+
|                                                                          |
|  Page title (28px)                                      [primary action] |
|  filters as pills (All, Available now, Fiction, ...)                     |
|                                                                          |
|  cover  cover  cover  cover  cover  cover        <- 6 columns at 1280    |
|  title  title  ...                                                       |
|  author  state                                                           |
+--------------------------------------------------------------------------+
```
- No sidebar. The dark left sidebar and dark top bar are removed. Navigation is the top bar only.
- Lead on every list page: the covers. Text under each cover is two lines (title, author) and one status line.
- Avatar menu holds Dashboard (my lendings), My collections, Profile, Admin (role only), Log out.
- Detail pages (book, collection): two columns at >= 960px (large cover left 360px, content right), stacked below.
- Cart and checkout: one column of items (max 720px) plus a sticky summary card at >= 960px.
- Below 720px: top bar shrinks to logo, search icon, cart, avatar; a bottom tab bar (Browse, Collections, Following, Me) appears (call).

## 3. Signature interaction
**Borrowing.** On a cover: hover lifts the cover 4px with a deeper shadow (transform + opacity only, 200ms). Pressing "Borrow" makes a small copy of the cover travel in an arc to the cart icon and shrink into it (450ms, transform + opacity), the cart badge pops once, and the button becomes "In your cart". This is the one place with a long animation. Everything else is 100 to 200ms feedback.
Reduced motion: no travel, no lift; the badge and button just update.

## 4. Color
Theme priority: **light first.** Dark is a later pass (call).

Surfaces (warm paper, 3 steps):
| Token | Value | Use |
|---|---|---|
| `--paper` | `#FAF9F4` | page background |
| `--card` | `#FFFFFF` | raised panels, inputs, popovers |
| `--sunk` | `#F1EFE7` | filter pills, inset areas, skeletons |

Text (3 greys only):
| Token | Value | on paper | on card |
|---|---|---|---|
| `--ink` | `#2B2722` | 14.07:1 | ~15:1 |
| `--ink-2` (secondary) | `#5E584D` | ~6.6:1 (verify) | ~7:1 |
| `--ink-3` (tertiary, captions) | `#726C60` | ~4.9:1 (verify; `#7A7468` measured 4.40, too low) | ~5.2:1 |

Lines: `--line` `#E7E3D8` hairline, used only for input outlines and the sticky header's bottom edge. No borders around cards.

**One accent: terracotta.**
| Token | Value | Contrast |
|---|---|---|
| `--accent` | `#B93A0B` | 5.42:1 on paper, 5.72:1 white text on it |
| `--accent-hover` | `#9E3009` | 7.29:1 white text on it |
| `--accent-tint` | `#F8E6DD` | accent text on it 4.72:1 |

The accent is used for: the primary button (one per view), the focus ring, the cart badge, active nav underline, links inside body text. Nothing else. Secondary buttons are ink-outline (1px `--ink`) or `--sunk` filled.

Status hues (only where they mean something):
| State | Text | Tint | Contrast |
|---|---|---|---|
| Available | `#1F6B45` | `#E3F1E8` | 5.55:1 |
| Waiting list / due soon | `#7A4F00` | `#F8EBCB` | 6.02:1 |
| Error / overdue | `#A8231C` | `#FBE4E1` | 5.92:1 |
| Unavailable / On loan | `--ink-2` | `--sunk` | 6+ |

Removed: all blue (`#3b82f6`, `#2563eb`), purple labels, rainbow sidebar icons, slate navy, any gradient, any glow.

## 5. Type
Two faces, one per job (call: global rule says one family; the serif is limited to book titles and the page's display line so it reads as content, not UI):
- **UI:** Schibsted Grotesk (variable, 400 / 500 / 600 / 700), self-hosted via fontsource.
- **Book titles and display:** Literata (variable, 500 / 600), same fontsource route.

Fixed scale (px / line-height / weight / tracking):
| Role | Size | LH | Weight | Tracking | Face |
|---|---|---|---|---|---|
| Display (home hero, book detail title) | 40 | 44 | 600 | -0.02em | Literata |
| Page title | 28 | 34 | 600 | -0.015em | Literata |
| Section title | 20 | 26 | 600 | -0.01em | Schibsted |
| Body | 15 | 24 | 400 | 0 | Schibsted |
| Card title (book) | 15 | 20 | 600 | 0 | Literata |
| UI label, button | 14 | 20 | 500 | 0 | Schibsted |
| Caption, status | 13 | 18 | 500 | 0.005em | Schibsted |
Minimum text size 13px. Numbers (prices, counts) use `font-variant-numeric: tabular-nums`.

## 6. Space, radius, borders
- Spacing scale (px): 4, 8, 12, 16, 24, 32, 48, 64. No other values.
- Radii: `--r-cover` 4px (covers look like books), `--r-control` 10px (buttons, inputs), `--r-surface` 16px (cards, popovers, modals), `--r-pill` 999px (search, filter pills, status chips, avatars).
- Borders: only input outlines and the header edge (`--line`). Cards separate by spacing and a surface step.
- Shadows (two only): `--shadow-1` `0 1px 2px rgba(43,39,34,.06), 0 2px 8px rgba(43,39,34,.06)` (raised card, resting); `--shadow-2` `0 4px 12px rgba(43,39,34,.10), 0 12px 32px rgba(43,39,34,.10)` (hover lift, popovers, modals). Covers rest on `--shadow-1`.

## 7. Density
- Controls: 44px tall (touch safe) for primary, 40px for secondary and inputs, 32px for chips.
- Cover grid: 6 columns at 1280, 5 at 1024, 4 at 768, 2 at 390; gap 24px horizontal, 40px vertical. Cover aspect 2:3.
- Tables (admin, lendings): 56px rows, no zebra stripes, hairline `--line` between rows only in tables.
- Status is always text plus tint, never colour alone.

## 8. Motion
- Easing tokens: `--ease-out` `cubic-bezier(0.2, 0, 0, 1)` (all UI; same curve Airbnb uses), `--ease-spring` `linear(0, 0.64 25%, 0.91 37.5%, 1.02 50%, 1.04 62.5%, 1)` (cart badge pop only).
- Durations: 120ms press and colour, 200ms hover lift and fades, 450ms borrow travel, 300ms for panels and menus.
- Transform and opacity only. No animated layout or box-shadow transitions on large lists (shadow swaps by opacity of a pseudo-element).
- Loading: skeleton covers on `--sunk` with one slow 1.4s opacity pulse, not a shimmer gradient.
- `prefers-reduced-motion: reduce`: remove travel, lift and pulse; keep instant state changes.

## 9. States to design
- Catalogue loaded with real covers and generated covers mixed.
- **Missing cover:** typographic cover on a muted paper tone (title in Literata, author small, deterministic tone from the title hash among 6 warm tones). Never a flat coloured square with a letter.
- Empty: no search results, empty cart, no collections, nobody followed, no notifications. Each gets a short sentence and one action.
- Loading (skeleton grid), error (inline message with retry), offline.
- Unavailable book: cover at full strength, status "Unavailable" (nobody lends it right now), "On loan" (yours, currently lent) or "Waiting list #3"; the button becomes "Notify me" or "Leave waiting list".
- Not logged in: Borrow opens login with a return path.
- Long titles: clamp to two lines on cards, never truncate on the detail page.
- Admin: tables with long emails, empty table.
- Payment: Stripe redirect pending, cancelled return, paid return.

## 10. Components to restyle first
1. Tokens (CSS variables, Tailwind theme), fonts, global focus ring.
2. Button, Input, Pill/Chip, StatusChip, Avatar.
3. BookCover (real image or generated) and BookCard, BookGrid, skeleton.
4. App shell: top bar, search pill, user menu, notifications menu, cart badge, mobile tab bar.
5. Books list page and filters.
6. Book detail (large cover, lending state, comments, votes).
7. Cart and checkout summary.
8. Collections (list, public, details, create).
9. Login and Register.
10. Dashboard (my lendings, queue), Profile, Following.
11. Modals and confirm dialogs, Pagination, toasts.
12. Admin tables.

## 11. Do and don't
Do:
- Let covers carry the page; give them the largest, quietest space.
- One primary action per view, in terracotta.
- Use real titles, authors and names in every mockup (the seeded catalogue has them).
- Keep status wording consistent: Available, Waiting list, Unavailable, On loan.

Don't:
- No sidebar, no dark navy bars, no gradients, no glow, no glassmorphism.
- No border plus shadow plus radius on every container.
- No emoji or letter-in-a-square placeholders.
- No blue, purple or green except the semantic status tints.
- No more than 3 text greys, no one-off font sizes or radii.

## 12. Decisions (owner, locked)
- Accent: **terracotta `#B93A0B`**.
- Theme priority: **light first** (dark deferred).
- Device priority: **desktop first** (1280 design width), clean collapse to phone.
- Reference: **Airbnb** (chosen by owner); Open Library added by Claude for the lending domain.
- Scope: complete redesign of every screen, not just colours.

## 13. Remaining (call) items
- Two faces (Schibsted Grotesk + Literata) versus a strict single family.
- Exact `--ink-2` and `--ink-3` values, to be measured in the browser during the build.
- Mobile bottom tab bar versus keeping the top bar only on phones.
- Dark theme later, and its accent (needs a lighter terracotta).
- Whether the home page for logged-out visitors becomes a catalogue-first landing (recommended) or keeps a hero.
- Fonts via fontsource npm packages (new dependency) versus Google Fonts link.
