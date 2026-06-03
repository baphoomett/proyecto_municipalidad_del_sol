# Heuristic unused import finder for Java files
Get-ChildItem -Path . -Recurse -Include *.java | ForEach-Object {
    $path = $_.FullName
    $text = Get-Content -Raw -LiteralPath $path -ErrorAction SilentlyContinue
    if (-not $text) { return }
    $imports = Select-String -InputObject $text -Pattern '^[ \t]*import\s+([^;]+);' -AllMatches | ForEach-Object { $_.Matches } | ForEach-Object { $_.Groups[1].Value }
    if ($imports.Count -eq 0) { return }
    foreach ($imp in $imports) {
        if ($imp -match '\*$') { continue }
        if ($imp -match '^static') { continue }
        $parts = $imp -split '\.'
        $simple = $parts[-1]
        $other = ($text -replace '^[ \t]*import\s+[^;]+;','', 'Singleline')
        if (-not ($other -match "\b$simple\b")) {
            Write-Output "$path :: $imp"
        }
    }
}
