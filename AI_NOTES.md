## Generowanie kodu

Agent AI był używany jako narzędzie wspomagające przy tworzeniu następujących elementów:

- Endpointy `addAccount` oraz `getAccountDetails` - wygenerowane na podstawie
  zaprojektowanej architektury systemu z narzuconymi regułami walidacji
- Eksport danych do CSV - poprzedzony researchem wzorców implementacji w Spring Boot
- Integracja OpenAPI + Swagger UI - poprzedzona researchem podejścia konfiguracyjnego
- Testy jednostkowe i integracyjne - przypadki testowe zaprojektowane przeze mnie,
  implementacja wspomagana przez agenta

**Cały kod wygenerowany przez agenta został manualnie przejrzany i zweryfikowany.**
W przypadkach niezgodności z założeniami projektu kod był przepisywany,
przykładem jest filtrowanie transakcji, które zostało przeprojektowane
z użyciem wzorca Specification.

## Dokumentacja

README zostało wygenerowane przez agenta AI na podstawie struktury projektu
i wymagań zadania. Treść i zgodność z projektem zweryfikowane przeze mnie.

## Research

Chatbot AI był wykorzystywany do researchu konceptów Spring Boot, bibliotek
oraz wzorców implementacyjnych używanych w projekcie.