#!/usr/bin/env python3
"""Converte data/players.csv em data/teams.json (formato lido pelo app).

Colunas do CSV: club,country,player,position,age,market_value_eur,overall,youth
- overall é opcional: se vazio, é estimado pelo valor de mercado.
- youth é opcional (1 = jogador da base).
A FONTE do CSV é sua responsabilidade: use dados cuja licença permita esse uso.
"""
import csv
import json
import math
import sys
from collections import OrderedDict

POS = {
    "GK": "GK", "GOLEIRO": "GK",
    "DEF": "DEF", "ZAGUEIRO": "DEF", "LATERAL": "DEF", "DEFENSOR": "DEF", "CB": "DEF", "LB": "DEF", "RB": "DEF",
    "MID": "MID", "MEIA": "MID", "MEIO-CAMPO": "MID", "VOLANTE": "MID", "CM": "MID", "DM": "MID", "AM": "MID",
    "ATT": "ATT", "ATACANTE": "ATT", "PONTA": "ATT", "CENTROAVANTE": "ATT", "FW": "ATT", "ST": "ATT",
}


def estimate_overall(value: float) -> int:
    v = max(value, 10_000)
    return max(35, min(95, round(70 + 10 * math.log10(v / 1_000_000))))


def to_int(s, default=None):
    try:
        return int(float(s))
    except (TypeError, ValueError):
        return default


def main(src="data/players.csv", dst="data/teams.json"):
    teams = OrderedDict()
    with open(src, newline="", encoding="utf-8") as f:
        for row in csv.DictReader(f):
            club = (row.get("club") or "").strip()
            if not club:
                continue
            t = teams.setdefault(club, {
                "id": "t%d" % len(teams), "name": club,
                "country": (row.get("country") or "").strip(),
                "squad": [], "youthSquad": [],
            })
            value = to_int(row.get("market_value_eur"), 0)
            overall = to_int(row.get("overall")) or estimate_overall(value)
            player = {
                "name": (row.get("player") or "").strip(),
                "position": POS.get((row.get("position") or "").strip().upper(), "MID"),
                "age": to_int(row.get("age"), 25),
                "overall": overall,
                "marketValueEur": value,
            }
            (t["youthSquad"] if (row.get("youth") or "").strip() == "1" else t["squad"]).append(player)

    out = [t for t in teams.values() if len(t["squad"]) >= 11]
    if len(out) < 2:
        sys.exit("Menos de 2 times com 11+ jogadores no CSV; nada gerado.")
    if len(out) % 2:
        print("AVISO: número ímpar de times (%d); o app exige número par." % len(out), file=sys.stderr)
    with open(dst, "w", encoding="utf-8") as f:
        json.dump(out, f, ensure_ascii=False, indent=1)
    print("%d times escritos em %s" % (len(out), dst))


if __name__ == "__main__":
    main(*sys.argv[1:])
