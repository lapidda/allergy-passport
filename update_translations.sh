#!/bin/bash

# Script to add missing translation keys to all language files
# This adds the keys that were added to the English, German, French, Spanish, and Italian files

cd "e:\DevStuff\AIllergenes\src\main\resources\i18n"

# Portuguese (pt)
sed -i '/^landing\.cta\.start=/a\
landing.cta.ready=Pronto para comer com segurança em qualquer lugar?\
landing.cta.subtitle=Crie seu passaporte de alergias gratuito em menos de 2 minutos.\
landing.cta.now=Comece agora' messages_pt.properties

sed -i '/^landing\.feature3\.desc=/a\
landing.allergens.title=Alergênos comuns que apoiamos' messages_pt.properties

sed -i '/^dashboard\.share\.copy=/a\
dashboard.share.preview=Visualizar página pública' messages_pt.properties

sed -i 's/^passport\.powered\.by=.*/passport.powered.by=Desenvolvido por Allergy Passport/' messages_pt.properties

sed -i '/^passport\.powered\.by=/a\
passport.create.own=Crie seu próprio passaporte gratuito' messages_pt.properties

# Russian (ru)
sed -i '/^landing\.cta\.start=/a\
landing.cta.ready=Готовы безопасно питаться где угодно?\
landing.cta.subtitle=Создайте свой бесплатный паспорт аллергий менее чем за 2 минуты.\
landing.cta.now=Начать сейчас' messages_ru.properties

sed -i '/^landing\.feature3\.desc=/a\
landing.allergens.title=Распространенные аллергены, которые мы поддерживаем' messages_ru.properties

sed -i '/^dashboard\.share\.copy=/a\
dashboard.share.preview=Предпросмотр публичной страницы' messages_ru.properties

sed -i 's/^passport\.powered\.by=.*/passport.powered.by=При поддержке Allergy Passport/' messages_ru.properties

sed -i '/^passport\.powered\.by=/a\
passport.create.own=Создайте свой собственный бесплатный паспорт' messages_ru.properties

# Chinese (zh)
sed -i '/^landing\.cta\.start=/a\
landing.cta.ready=准备好在任何地方安全用餐了吗？\
landing.cta.subtitle=在不到2分钟内创建您的免费过敏护照。\
landing.cta.now=立即开始' messages_zh.properties

sed -i '/^landing\.feature3\.desc=/a\
landing.allergens.title=我们支持的常见过敏原' messages_zh.properties

sed -i '/^dashboard\.share\.copy=/a\
dashboard.share.preview=预览公开页面' messages_zh.properties

sed -i 's/^passport\.powered\.by=.*/passport.powered.by=由 Allergy Passport 提供支持/' messages_zh.properties

sed -i '/^passport\.powered\.by=/a\
passport.create.own=创建您自己的免费护照' messages_zh.properties

# Japanese (ja)
sed -i '/^landing\.cta\.start=/a\
landing.cta.ready=どこでも安全に食事をする準備はできましたか？\
landing.cta.subtitle=2分以内に無料のアレルギーパスポートを作成できます。\
landing.cta.now=今すぐ始める' messages_ja.properties

sed -i '/^landing\.feature3\.desc=/a\
landing.allergens.title=対応している一般的なアレルゲン' messages_ja.properties

sed -i '/^dashboard\.share\.copy=/a\
dashboard.share.preview=公開ページをプレビュー' messages_ja.properties

sed -i 's/^passport\.powered\.by=.*/passport.powered.by=Allergy Passport による提供/' messages_ja.properties

sed -i '/^passport\.powered\.by=/a\
passport.create.own=無料のパスポートを作成' messages_ja.properties

# Korean (ko)
sed -i '/^landing\.cta\.start=/a\
landing.cta.ready=어디서든 안전하게 식사할 준비가 되셨나요?\
landing.cta.subtitle=2분 이내에 무료 알레르기 여권을 만드세요.\
landing.cta.now=지금 시작하기' messages_ko.properties

sed -i '/^landing\.feature3\.desc=/a\
landing.allergens.title=지원하는 일반적인 알레르겐' messages_ko.properties

sed -i '/^dashboard\.share\.copy=/a\
dashboard.share.preview=공개 페이지 미리보기' messages_ko.properties

sed -i 's/^passport\.powered\.by=.*/passport.powered.by=Allergy Passport에서 제공/' messages_ko.properties

sed -i '/^passport\.powered\.by=/a\
passport.create.own=무료 여권 만들기' messages_ko.properties

# Dutch (nl)
sed -i '/^landing\.cta\.start=/a\
landing.cta.ready=Klaar om overal veilig te eten?\
landing.cta.subtitle=Maak uw gratis allergiepaspoort in minder dan 2 minuten.\
landing.cta.now=Nu beginnen' messages_nl.properties

sed -i '/^landing\.feature3\.desc=/a\
landing.allergens.title=Veelvoorkomende allergenen die we ondersteunen' messages_nl.properties

sed -i '/^dashboard\.share\.copy=/a\
dashboard.share.preview=Voorvertoning publieke pagina' messages_nl.properties

sed -i 's/^passport\.powered\.by=.*/passport.powered.by=Mogelijk gemaakt door Allergy Passport/' messages_nl.properties

sed -i '/^passport\.powered\.by=/a\
passport.create.own=Maak uw eigen gratis paspoort' messages_nl.properties

# Polish (pl)
sed -i '/^landing\.cta\.start=/a\
landing.cta.ready=Gotowy na bezpieczne jedzenie wszędzie?\
landing.cta.subtitle=Utwórz swój darmowy paszport alergiczny w mniej niż 2 minuty.\
landing.cta.now=Zacznij teraz' messages_pl.properties

sed -i '/^landing\.feature3\.desc=/a\
landing.allergens.title=Wspierane powszechne alergeny' messages_pl.properties

sed -i '/^dashboard\.share\.copy=/a\
dashboard.share.preview=Podgląd strony publicznej' messages_pl.properties

sed -i 's/^passport\.powered\.by=.*/passport.powered.by=Obsługiwane przez Allergy Passport/' messages_pl.properties

sed -i '/^passport\.powered\.by=/a\
passport.create.own=Utwórz własny darmowy paszport' messages_pl.properties

# Swedish (sv)
sed -i '/^landing\.cta\.start=/a\
landing.cta.ready=Redo att äta säkert var som helst?\
landing.cta.subtitle=Skapa ditt gratis allergipass på mindre än 2 minuter.\
landing.cta.now=Börja nu' messages_sv.properties

sed -i '/^landing\.feature3\.desc=/a\
landing.allergens.title=Vanliga allergener vi stöder' messages_sv.properties

sed -i '/^dashboard\.share\.copy=/a\
dashboard.share.preview=Förhandsgranska offentlig sida' messages_sv.properties

sed -i 's/^passport\.powered\.by=.*/passport.powered.by=Drivs av Allergy Passport/' messages_sv.properties

sed -i '/^passport\.powered\.by=/a\
passport.create.own=Skapa ditt eget gratis pass' messages_sv.properties

# Danish (da)
sed -i '/^landing\.cta\.start=/a\
landing.cta.ready=Klar til at spise sikkert hvor som helst?\
landing.cta.subtitle=Opret dit gratis allergipass på under 2 minutter.\
landing.cta.now=Begynd nu' messages_da.properties

sed -i '/^landing\.feature3\.desc=/a\
landing.allergens.title=Almindelige allergener vi understøtter' messages_da.properties

sed -i '/^dashboard\.share\.copy=/a\
dashboard.share.preview=Forhåndsvisning af offentlig side' messages_da.properties

sed -i 's/^passport\.powered\.by=.*/passport.powered.by=Drevet af Allergy Passport/' messages_da.properties

sed -i '/^passport\.powered\.by=/a\
passport.create.own=Opret dit eget gratis pas' messages_da.properties

# Norwegian (no)
sed -i '/^landing\.cta\.start=/a\
landing.cta.ready=Klar til å spise trygt hvor som helst?\
landing.cta.subtitle=Lag ditt gratis allergipass på under 2 minutter.\
landing.cta.now=Begynn nå' messages_no.properties

sed -i '/^landing\.feature3\.desc=/a\
landing.allergens.title=Vanlige allergener vi støtter' messages_no.properties

sed -i '/^dashboard\.share\.copy=/a\
dashboard.share.preview=Forhåndsvisning av offentlig side' messages_no.properties

sed -i 's/^passport\.powered\.by=.*/passport.powered.by=Drevet av Allergy Passport/' messages_no.properties

sed -i '/^passport\.powered\.by=/a\
passport.create.own=Lag ditt eget gratis pass' messages_no.properties

# Finnish (fi)
sed -i '/^landing\.cta\.start=/a\
landing.cta.ready=Valmis syömään turvallisesti missä tahansa?\
landing.cta.subtitle=Luo ilmainen allergipassisi alle 2 minuutissa.\
landing.cta.now=Aloita nyt' messages_fi.properties

sed -i '/^landing\.feature3\.desc=/a\
landing.allergens.title=Tuemme yleisiä allergeeneja' messages_fi.properties

sed -i '/^dashboard\.share\.copy=/a\
dashboard.share.preview=Esikatsele julkista sivua' messages_fi.properties

sed -i 's/^passport\.powered\.by=.*/passport.powered.by=Tarjoaa Allergy Passport/' messages_fi.properties

sed -i '/^passport\.powered\.by=/a\
passport.create.own=Luo oma ilmainen passisi' messages_fi.properties

# Greek (el)
sed -i '/^landing\.cta\.start=/a\
landing.cta.ready=Έτοιμοι να φάτε με ασφάλεια οπουδήποτε;\
landing.cta.subtitle=Δημιουργήστε το δωρεάν διαβατήριο αλλεργιών σας σε λιγότερο από 2 λεπτά.\
landing.cta.now=Ξεκινήστε τώρα' messages_el.properties

sed -i '/^landing\.feature3\.desc=/a\
landing.allergens.title=Κοινά αλλεργιογόνα που υποστηρίζουμε' messages_el.properties

sed -i '/^dashboard\.share\.copy=/a\
dashboard.share.preview=Προεπισκόπηση δημόσιας σελίδας' messages_el.properties

sed -i 's/^passport\.powered\.by=.*/passport.powered.by=Υποστηρίζεται από Allergy Passport/' messages_el.properties

sed -i '/^passport\.powered\.by=/a\
passport.create.own=Δημιουργήστε το δικό σας δωρεάν διαβατήριο' messages_el.properties

# Hindi (hi)
sed -i '/^landing\.cta\.start=/a\
landing.cta.ready=कहीं भी सुरक्षित रूप से खाने के लिए तैयार हैं?\
landing.cta.subtitle=2 मिनट से कम समय में अपना निःशुल्क एलर्जी पासपोर्ट बनाएं।\
landing.cta.now=अभी शुरू करें' messages_hi.properties

sed -i '/^landing\.feature3\.desc=/a\
landing.allergens.title=सामान्य एलर्जेंस जिनका हम समर्थन करते हैं' messages_hi.properties

sed -i '/^dashboard\.share\.copy=/a\
dashboard.share.preview=सार्वजनिक पृष्ठ का पूर्वावलोकन' messages_hi.properties

sed -i 's/^passport\.powered\.by=.*/passport.powered.by=Allergy Passport द्वारा संचालित/' messages_hi.properties

sed -i '/^passport\.powered\.by=/a\
passport.create.own=अपना निःशुल्क पासपोर्ट बनाएं' messages_hi.properties

# Turkish (tr)
sed -i '/^landing\.cta\.start=/a\
landing.cta.ready=Her yerde güvenle yemek yemeye hazır mısınız?\
landing.cta.subtitle=2 dakikadan kısa sürede ücretsiz alerji pasaportunuzu oluşturun.\
landing.cta.now=Şimdi başlayın' messages_tr.properties

sed -i '/^landing\.feature3\.desc=/a\
landing.allergens.title=Desteklediğimiz yaygın alerjenler' messages_tr.properties

sed -i '/^dashboard\.share\.copy=/a\
dashboard.share.preview=Halka açık sayfayı önizle' messages_tr.properties

sed -i 's/^passport\.powered\.by=.*/passport.powered.by=Allergy Passport tarafından desteklenmektedir/' messages_tr.properties

sed -i '/^passport\.powered\.by=/a\
passport.create.own=Kendi ücretsiz pasaportunuzu oluşturun' messages_tr.properties

# Arabic (ar)
sed -i '/^landing\.cta\.start=/a\
landing.cta.ready=هل أنت مستعد لتناول الطعام بأمان في أي مكان؟\
landing.cta.subtitle=أنشئ جواز سفر الحساسية المجاني الخاص بك في أقل من دقيقتين.\
landing.cta.now=ابدأ الآن' messages_ar.properties

sed -i '/^landing\.feature3\.desc=/a\
landing.allergens.title=مسببات الحساسية الشائعة التي ندعمها' messages_ar.properties

sed -i '/^dashboard\.share\.copy=/a\
dashboard.share.preview=معاينة الصفحة العامة' messages_ar.properties

sed -i 's/^passport\.powered\.by=.*/passport.powered.by=مدعوم من Allergy Passport/' messages_ar.properties

sed -i '/^passport\.powered\.by=/a\
passport.create.own=أنشئ جواز سفرك المجاني الخاص' messages_ar.properties

echo "All language files updated successfully!"
