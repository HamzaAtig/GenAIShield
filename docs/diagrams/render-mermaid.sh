#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
DIAGRAMS_DIR="$ROOT_DIR/docs/diagrams"

if ! command -v npx >/dev/null 2>&1; then
  echo "npx is required (Node.js/npm)."
  exit 1
fi

echo "Rendering Mermaid diagrams..."
npx -y @mermaid-js/mermaid-cli \
  -i "$DIAGRAMS_DIR/use-cases-class-diagram.mmd" \
  -o "$DIAGRAMS_DIR/use-cases-class-diagram.svg" \
  -b transparent

echo "Done: $DIAGRAMS_DIR/use-cases-class-diagram.svg"
