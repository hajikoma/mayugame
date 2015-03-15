package com.hajikoma.mayugame;

import java.io.Serializable;

import com.hajikoma.mayugame.item.DefaultPicker;

/**
 * ユーザーのデータを格納するクラス。 データの保存時にはこのクラスを直列化する。
 * データがおかしくなるとユーザーは大変なストレスを感じるため、このクラスの設計は データの安全性を優先して設計する。
 * ・シングルトンなクラスとする（インスタンスの取得はgetUserDataメソッドで行う）
 * ・全てのフィールド変数をカプセル化し、規定範囲外の値が入らないようにする
 * ・staticな列挙型による定数管理 ・例外処理の追加
 * またこのクラスインスタンスへのアクセス数は極力少ない方が良い。
 */
public class UserData implements Serializable {

	/** serialVersionUID(YYYYMMDDHHMM+L) */
	private static final long serialVersionUID = 201502212356L;

    /** ミッションの状況を表す定数（未開放） */
	public static final int MIS_LOCKED = -1;
    /** ミッションの状況を表す定数（開放済み、未挑戦） */
	public static final int MIS_UNLOCKED = 0;
    /** ミッションの状況を表す定数（失敗） */
	public static final int MIS_BAD = 1;
    /** ミッションの状況を表す定数（成功） */
	public static final int MIS_GOOD = 2;
    /** ミッションの状況を表す定数（大成功） */
	public static final int MIS_EXCELLENT = 3;

    /** 唯一のUserDataインスタンス */
    private static UserData ud = new UserData();

    /** ミッションの達成状況 */
    private int[] misState = new int[36];
    /** 最後に挑戦したミッションのインデックス。-1は初回実行時 */
    private int lastChallengeMis = -1;
    /** アイテムの所有状況（-1は未開放） */
    private int[] itemState = new int[30];
    /** 「お手入れで使う」ように設定されたアイテムのインデックス */
    private int useItemIndex = 0;

    /** 所有まゆポイント */
    private int totalMP = 0;
    /** 抜いた本数 */
    private int pickCount = 0;
    /** 自然に抜け落ちた本数 */
    private int dropCount = 0;
    /** ちぎれた本数 */
    private int tornCount = 0;
    /** 抜いた総延長 */
    private int totalLength = 0;

    /** フリック能力（当たり判定の広さ） */
    private int expansion = 20;
    /** フリック能力（ちぎれにくさ */
    private int torning = 30;
    /** フリック能力（まゆポイント倍率） */
    private float pointPar = 1.0f;

    /** 設定の状況（ミュート） */
    private boolean mute = false;
    /** 設定の状況（バイブレーション） */
	private boolean vibe = true;


    /**
	 * コンストラクタ。このクラスはシングルトンなクラスなので、このメソッドは外部から利用できない。
     * ミッションとアイテムの初期化を行う。
     */
    private UserData(){
    	//ポイントの初期化
    	setTotalMP(1000);

    	//ミッションの初期化
    	for(int i = 0; i < 36; i++){
    		if(i % 6 == 0){
        		misState[i] = 0;
    		}else{
        		misState[i] = -1;
    		}
    	}

    	//アイテムの初期化
    	for(int i = 0; i < 30; i++){
    		itemState[i] = -1;
    	}
    	itemState[0] = 5;
    }

    /** UserDataインスタンスを取得する。複数回このメソッドを実行しても返されるインスタンスは同一である。 */
    public static UserData getUserData(){
        return ud;
    }

    /**
     * 特定のミッションの達成状況を取得する。
     * @param misIndex ミッション番号
     * @return ミッションの達成状況
     */
    public int getMisState(int misIndex) {
        return misState[misIndex];
    }

    /**
     * 全てのミッションの達成状況を取得する。
     * @return 全ミッションの達成状況配列
     */
    public int[] getAllMisState() {
        return misState;
    }

    /**
     * ミッションのロックを外し、開放する。
     * @param misNo 開放するミッション番号
     * @throws java.lang.IndexOutOfBoundsException 存在しないmisNoを指定した場合
     */
    public void misUnLock(int misNo) {
    	if(misNo >= 0 && misNo < Assets.mission_list.size()){
            misState[misNo] = 0;
    	}else{
    		throw new IndexOutOfBoundsException("存在しないmisNoです。");
    	}
    }

    /**
     * ミッションの挑戦結果を格納する。
     * @param misNo 結果を格納するミッション番号
     * @throws java.lang.IllegalArgumentException 引数resultにlocked,unlockedを指定した場合
     * @throws java.lang.IndexOutOfBoundsException 存在しないmisNoを指定した場合
     */
    public void setMisResult(int misNo, int result) {
    	if(misNo >= 0 && misNo < misState.length){
    		if(result > 0){
    			misState[misNo] = result;
    		}else{
        		throw new IllegalArgumentException("ミッションのlock,unlockをこのメソッドで変更することはできません。");
    		}
    	}else{
    		throw new IndexOutOfBoundsException("存在しないmisNoです。");
    	}
    }

    /** 最後に挑戦したミッション番号を取得する */
	public int getLastChallengeMis() {
		return lastChallengeMis;
	}

    /**
     * 最後に挑戦したミッション番号を格納する。
     * @param misNo 最後に挑戦したミッション番号
     * @throws java.lang.IndexOutOfBoundsException 存在しないmisNoを指定した場合
     */
	public void setLastChallengeMis(int misNo) {
    	if(misNo >= 0 && misNo < misState.length){
    		lastChallengeMis = misNo;
    	}else{
    		throw new IndexOutOfBoundsException("存在しないmisNoです。");
    	}
	}

	/**
	 * すべてのアイテムの所有状況を取得する。
	 * @return すべてのアイテムの所有状況配列
	 */
	public int[] getAllItemState() {
		return itemState;
	}

    /**
     * 特定のアイテムの所有状況を取得する。
     * @param itemNo 状況を取得するアイテム番号
     * @throws java.lang.IndexOutOfBoundsException 存在しないitemNoを指定した場合
     */
	public int getItemState(int itemNo){
    	if(itemNo >= 0 && itemNo < itemState.length){
    		return itemState[itemNo];
    	}else{
    		throw new IndexOutOfBoundsException("存在しないitemNoです。");
    	}
	}

    /**
     * アイテムのロックを外し、開放する。
     * @param itemNo 開放するアイテム番号
     * @throws java.lang.IndexOutOfBoundsException 存在しないitemNoを指定した場合
     */
	public void itemUnlock(int itemNo) {
    	if(itemNo >= 0 && itemNo < itemState.length){
    		itemState[itemNo] = 0;
    	}else{
    		throw new IndexOutOfBoundsException("存在しないitemNoです。");
    	}
	}

    /**
     * アイテムの所有数を増減させる。
     * @param itemNo 増減させるアイテム番号
     * @param addNumber 増減数
     * @return 増減後のアイテム所有数
     * @throws java.lang.IndexOutOfBoundsException 所有数が負の値になるaddNumberを指定した、または存在しないitemNoを指定した場合
     */
	public int setItemNumber(int itemNo, int addNumber) {
    	if(itemNo >= 0 && itemNo < itemState.length){
    		if(itemState[itemNo] + addNumber >= 0){
    			itemState[itemNo] += addNumber;
        		return itemState[itemNo];
    		}else{
        		throw new IndexOutOfBoundsException("所有数が負の値になるaddNumberが与えられました。");
    		}
    	}else{
    		throw new IndexOutOfBoundsException("存在しないitemNoです。");
    	}
	}

	public int getUseItemIndex() {
		return useItemIndex;
	}

    /**
     *  「お手入れで使う」ように設定されたアイテムのインデックスを設定する
     * @param itemNo アイテムのインデックス
     * @throws java.lang.IndexOutOfBoundsException 存在しないitemNoを指定した場合
     */
	public void setUseItemIndex(int itemNo) {
    	if(itemNo >= 0 && itemNo < itemState.length){
    		useItemIndex = itemNo;
    	}else{
    		throw new IndexOutOfBoundsException("存在しないitemNoです。");
    	}
	}

	public int getTotalMP() {
		return totalMP;
	}

	/**
	 * 所有まゆポイントを増減させる。まゆポイントがマイナスになる場合、まゆポイントの増減は行われずにfalseを返す。
	 * アイテム購入時などは、この戻り値を判定する。
	 * @return 増減後のまゆポイントが負の値になる場合false
	 */
	public boolean setTotalMP(int addMP) {
		if(totalMP + addMP >= 0){
			totalMP += addMP;
			return true;
		}else{
			return false;
		}
	}

	public int getPickCount() {
		return pickCount;
	}

    /**
     * まゆ毛を抜いた総数を増加させる。
     * @param addCount 増加させる値
     * @return 増加後のまゆ毛を抜いた総数
     */
	public int addPickCount(int addCount) {
		pickCount += addCount;
		return pickCount;
	}

	public int getDropCount() {
		return dropCount;
	}

    /**
     * まゆ毛が自然に抜け落ちた総数を増加させる。
     * @param addCount 増加させる値
     * @return 増加後のまゆ毛が自然に抜け落ちた総数
     */
	public int addDropCount(int addCount) {
		dropCount += addCount;
		return dropCount;
	}

	public int getTornCount() {
		return tornCount;
	}

    /**
     * まゆ毛がちぎれた総数を増加させる。
     * @param addCount 増加させる値
     * @return 増加後のまゆ毛がちぎれた総数
     */
	public int addTornCount(int addCount) {
		tornCount += addCount;
		return tornCount;
	}

	public int getTotalLength() {
		return totalLength;
	}

    /**
     * 抜いたまゆ毛の総延長を加算する
     * @param addLength 加算する値
     * @return 加算後の抜いたまゆ毛の総延長
     */
	public int setTotalLength(int addLength) {
		totalLength += addLength;
		return totalLength;
	}

	public int getExpansion() {
		return expansion;
	}

	/**
	 * デフォルトの抜き方の能力値をユーザーデータのフィールドに格納し、両者を一致させる。
	 * 能力値を強化した後に必ず呼び出す必要がある。
	 * @param picker デフォルトの抜き方のインスタンス
	 */
	public void syncUserDataWithDefaultPicker(DefaultPicker picker) {
		expansion = picker.getExpansion();
		torning = picker.getTorning();
		pointPar = picker.getPointPar();
	}

	public int getTorning() {
		return torning;
	}

	public float getPointPar() {
		return pointPar;
	}

	public boolean isMute() {
		return mute;
	}

	public void mute() {
		mute = true;
	}

	public void unMute() {
		mute = false;
	}

	public boolean isVibeOn() {
		return vibe;
	}

	public void vibeOn() {
		vibe = true;
	}

	public void vibeOff() {
		vibe = false;
	}
}
