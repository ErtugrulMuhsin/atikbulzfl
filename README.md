# Atık Bul ZFL

Atık Bul ZFL, Zonguldak'taki geri dönüşüm ve atık toplama noktalarını harita üzerinde göstermeyi amaçlayan bir Android uygulamasıdır. Proje, Zonguldak Fen Lisesi öğrencileri tarafından Liselerde Bilim Uygulamaları Yarışması için geliştirilmiştir.

## Amaç

Geri dönüşüm noktalarına ulaşmayı kolaylaştırmak, atık türüne göre uygun toplama yerlerini göstermek ve kullanıcıların en yakın noktayı hızlıca bulmasını sağlamak.

## Özellikler

- Zonguldak merkezli atık toplama noktalarını haritada görüntüleme
- Atık türüne göre filtreleme: atık pil, kağıt, plastik, cam ve yağ
- Kullanıcının konumuna göre en yakın toplama noktasını bulma
- Material Design 3 temelli modern arayüz
- Açık ve koyu tema seçimi
- Koyu temada koyu harita görünümü
- GitHub deposuna uygulama içinden erişim
- Yeni konum önerileri ve geri bildirim için e-posta bağlantısı

## Kurulum

Uygulamayı kullanmak için GitHub Releases sayfasından en güncel APK dosyasını indirin:

[Releases sayfasına git](https://github.com/ErtugrulMuhsin/atikbulzfl/releases)

İndirdiğiniz APK dosyasını Android cihazınıza aktararak kurabilirsiniz. Android, bilinmeyen kaynaklardan uygulama yükleme izni isteyebilir; kurulum sırasında ekrandaki yönlendirmeleri takip edin.

## Geri Bildirim ve Konum Önerileri

Yeni bir atık toplama noktası ekletmek, hatalı bilgi bildirmek veya öneri paylaşmak için bize ulaşabilirsiniz:

**E-posta:** [atikbulzfl@tutamail.com](mailto:atikbulzfl@tutamail.com)

## Teknolojiler

- Java
- Android SDK
- Material Components for Android
- osmdroid
- Gradle
- OpenStreetMap tabanlı harita verileri

## Katkıda Bulunma ve Bilgisayarda Derleme

Projeyi geliştirmek veya kendi bilgisayarınızda derlemek için depoyu klonlayın:

```bash
git clone https://github.com/ErtugrulMuhsin/atikbulzfl.git
cd atikbulzfl
```

Android Studio ile açıp Gradle senkronizasyonunu tamamladıktan sonra uygulamayı çalıştırabilirsiniz.

Komut satırından debug APK oluşturmak için:

```bash
./gradlew assembleDebug
```

Windows üzerinde:

```powershell
.\gradlew.bat assembleDebug
```

Oluşan APK dosyası genellikle şu konumdadır:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Geliştiriciler

- Ertuğrul Muhsin DANACI
- Furkan Yiğit ÖZDEN
- Kayra Mehdi ARMAN
- Eslem Sibel YILDIRIM
- Toprak AÇIL
- Emir Kuzey GİDİCİ
- Ayşe SAV
- Poyraz ŞEKERCİ

## Yarışma

Bu proje, Zonguldak Fen Lisesi öğrencileri tarafından Liselerde Bilim Uygulamaları Yarışması kapsamında hazırlanmıştır.

## Lisans

Bu proje Apache License 2.0 ile lisanslanmıştır. Ayrıntılar için `LICENSE` dosyasına bakabilirsiniz.
