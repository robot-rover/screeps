check:
    ./gradlew check

deploy:
    ./gradlew deploy

make-venv:
    python -m venv .venv

strip:
    rg "require\('\./([^.']*)\.js'\)" build/js/packages/screeps/kotlin --files-with-matches | xargs sed -E -i "s/require\('\.\/([^.']+).js'\)/require('\1')/g"