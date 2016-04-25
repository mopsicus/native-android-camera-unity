using UnityEngine;
using System.Collections;
using UnityEngine.UI;
using System.IO;

public class ImagesController : MonoBehaviour {

	public Image Preview;

	public void GetImage () {
		#if UNITY_ANDROID
		using (var plugin = new AndroidJavaClass("com.mycompany.images.Plugin")) {
			plugin.CallStatic("launch");
		}
		#endif
	}

	void OnImageReceived (string path) {
		Texture2D texture = null;
		byte[] fileData = File.ReadAllBytes(path);
		texture = new Texture2D(2, 2);
		texture.LoadImage(fileData); 
		Preview.sprite = Sprite.Create(texture, new Rect(0, 0, texture.width, texture.height), new Vector2(.5f, .5f));
	}
}
