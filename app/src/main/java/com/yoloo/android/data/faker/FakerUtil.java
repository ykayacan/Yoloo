package com.yoloo.android.data.faker;

import android.support.annotation.NonNull;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Random;

public class FakerUtil {

  private static final String[] CONTENTS = {
      "Merhaba yakın zamanda yola çıkacağız,rotayı Prag-Amsterdam-Paris olarak düşündük,"
          + "mutlaka burayı da görün dediğiniz yerler varsa yazarsanız çok hayra geçersiniz. "
          + "Yemekler konusunda da tavsiyeleriniz olursa mutlaka söyleyin.",

      "Fransada bir arkadasim rusyaya gitmek icin bilet almis airbnb rezervasyonu yapmis falan"
          + " (ivir zivirlari tam yani). Lakin konsolosluk anlasmali oldugu acentalardan "
          + "VOUCHER istemis. Gel gelelim fransadaki acentalarda turk oldugu icin vermemisler "
          + "turkiyedekilerde yolunacak gurbetciyi gorup 200$ istemisler (ucak bileti daha ucuz)."
          + " Neyse napcaz simdi ? Nasil oluyor bu isler var midir bilen.",

      "Merhabalar \uD83D\uDE0A\n"
          + "İki enstrüman iki nefes düştük avrupa yollarına :)\n"
          + "Bu gece Berlinde bizi ağırlayabilecek dostlara ihtiyacımız var\uD83D\uDE07\n"
          + "Yardımcı olabilecek varsa çok mutlu oluruz \n"
          + "Kendisinin kulağının pasını silmeye kefiliz :)",

      "Merhaba, bilen biri bana açıklayabilir mi öğretebilir mi acaba Azerbaycan'a karayolu ile "
          + "gitmek için Ankara konsolosluğundan nasıl randevu alınıyor? Ne kadar ücret? "
          + "Hangi evrak istiyorlar? Google'da aradım bilgi kirliliği net bi "
          + "cevap alamadım. Bi el atın lütfen.",

      "Arkadaşlar merhaba, \n"
          + "Bu yaz için plan yapıyorum toplamda 12 Ülke\uD83C\uDDEA\uD83C\uDDFA, 27 "
          + "Şehir\uD83C\uDF03 ve 45 Günden\uD83C\uDF05 oluşan bir plan hazırladım aşağıda "
          + "görebilirsiniz sırasıyla⤵️ Planıma bakıp bu güzergaha göre \"aa bu şehri boşver "
          + "gitme\" ya da \"aaa bu şehre mutlaka gitmelisin ekle bence\" dediğiniz değişiklikler"
          + "varsa tavsiyelere açığım. Aynı zamanda bazı şehirlere 1 günden fazla zaman "
          + "ayırabiliyorum, zamanımı sizce nerelerde daha uzun geçirmeliyim? Kaç gün? Bu plan "
          + "için her türlü tavsiyelerinizi bekliyorum\uD83D\uDE47 Bir de sizce böyle bir programa"
          + " toplamda ne kadar bütçe ayırmalıyım?\uD83D\uDCB6 \n"
          + "•\n"
          + "•\n"
          + "•\n"
          + "Istanbul ✈️ Napoli \uD83D\uDE8C Roma \uD83D\uDE8C Floransa \uD83D\uDE8C "
          + "Venedik \uD83D\uDE8C Ljubliana \uD83D\uDE8C Graz \uD83D\uDE8C Viyana \uD83D\uDE8C "
          + "Brno \uD83D\uDE8C Prag \uD83D\uDE8C Berlin \uD83D\uDE8C Hamburg \uD83D\uDE8C "
          + "Köln \uD83D\uDE8C Giethoorn \uD83D\uDE8C Amsterdam \uD83D\uDE8C Rotterdam \uD83D\uDE8C "
          + "Antwerp \uD83D\uDE8C Brüksel \uD83D\uDE8C Paris \uD83D\uDE8C Zürih ✈️ Valencia "
          + "\uD83D\uDE8C Madrid \uD83D\uDE8C Lizbon ✈️ Paris \uD83D\uDE84 Londra \uD83D\uDE8C"
          + " Oxford \uD83D\uDE8C Bristol \uD83D\uDE8C Bournemouth \uD83D\uDE8C Eastbourne \uD83D\uDE8C "
          + "Londra ✈️ Istanbul\n"
          + "Gelecekten gelen not: Lizbon'dan direkt olarak londraya uçabilirim ama Manş "
          + "tünelinden trenle geçmek istediğim için Paris'e uçup geçmek istiyorum "
          + "Ingiltere'ye\uD83D\uDE4C\n"
          + "Bir de eğer tavsiyeleriniz yoksa up'larsanız belki daha çok insana ulaşabilirim, "
          + "teşekkürler\uD83D\uDE0A\n"
          + "Güncelleme1 Barcelona ve Brugge'i ekledim\uD83E\uDD13\n"
          + "Güncelleme2 Zurih'den Barcelona'ya uçucam oradan Madrid ve Sevilla yapıcam sırasıyla, "
          + "Valencia'yı çıkardım\uD83C\uDF83",

      "Selam arkadaşlar Panama da yaşıyorum Almanya'ya elçiliğinden vize alacağım (turistik) "
          + "bu vize ile Almanya'ya değilde İspanya veya Fransa'ya giriş yaparsam problem çıkar "
          + "mı yanlış bilmiyorsam aldığın yerden dolayı vizeyi ilk aldığın ülkeye gitmen "
          + "gerekiyor Muş ::teşekkürler",

      "Merhaba, Heidelberg Almanya'da iki kisilik kalacak daireye ihtiyacimiz var. "
          + "Ogrenci oldugumuzdan kiraya hersey dahil maksimum 500 Euro gibi bir "
          + "butce ayirabiliyoruz. Wg gesucht, immobilienscout ve airbnb sitelerini talan "
          + "ettik fakat uygun bir yer bulamiyoruz. Bu konuda bize yardimci olabilir misiniz?"
  };

  @NonNull
  public static String getMediaUrl() {
    return "https://unsplash.it/400/300/?random";
  }

  @NonNull
  public static String getContent() {
    int rand = new Random().nextInt(CONTENTS.length);
    return CONTENTS[rand];
  }

  public static int generateNumber() {
    return new Random().nextInt(3250);
  }

  public static String getAvatarRandomUrl() {
    int rand = new Random().nextInt(85);
    return "https://randomuser.me/api/portraits/med/men/" + rand + ".jpg";
  }

  public static Date getRandomDate() {
    final long beginTime = Timestamp.valueOf("2017-01-13 00:00:00").getTime();
    final long endTime = Timestamp.valueOf("2017-01-14 00:58:00").getTime();
    long diff = endTime - beginTime + 1;

    long timeStamp = beginTime + (long) (Math.random() * diff);
    return new Date(timeStamp);
  }

  public static String getFemaleAvatarUrl() {
    final int rand = new Random().nextInt(85);
    return "https://randomuser.me/api/portraits/med/women/" + rand + ".jpg";
  }

  public static String getMaleAvatarUrl() {
    final int rand = new Random().nextInt(85);
    return "https://randomuser.me/api/portraits/med/men/" + rand + ".jpg";
  }
}
