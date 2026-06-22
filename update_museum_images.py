import json
import os

base = os.path.join("app", "src", "main", "assets")
imgs_dir = os.path.join(base, "museum_images")

mapping = {
    1: "부산현대미술관 (해운대구).jpg",
    2: "F1963 (수영구).jpg",
    3: "부산시립미술관 (해운대구).jpg",
    4: "고은사진미술관 (해운대구).jpg",
    5: "아르떼뮤지엄 부산 (영도구).jpg",
    6: "부산근대역사관 (중구).jpg",
    7: "민주공원 (중구).jpg",
    8: "금정문화회관 (금정구).jpg",
}

json_files = ["museums.json", "museums_en.json", "museums_ja.json", "museums_zh.json"]

if not os.path.isdir(imgs_dir):
    raise SystemExit(f"Missing images directory: {imgs_dir}")

files = os.listdir(imgs_dir)
missing = [v for v in mapping.values() if v not in files]
print("missing files:", missing)

for jf in json_files:
    path = os.path.join(base, jf)
    with open(path, "r", encoding="utf-8") as f:
        data = json.load(f)
    for item in data:
        iid = item.get("id")
        if iid not in mapping:
            raise SystemExit(f"Unknown museum id: {iid}")
        item["imageUrl"] = f"file:///android_asset/museum_images/{mapping[iid]}"
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, indent=2)
    print("updated", jf)
